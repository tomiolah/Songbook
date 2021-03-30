import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from "rxjs/Subscription";
import { Song, SongService } from "../../services/song-service.service";
import { AuthService } from "../../services/auth.service";
import { ActivatedRoute, Router } from "@angular/router";
import { SongLink } from "../../models/song-link";
import { SongLinkDataService } from "../../services/song-link-data.service";
import { Title } from "@angular/platform-browser";
import { AuthenticateComponent } from "../authenticate/authenticate.component";
import { MatDialog } from "@angular/material";

@Component({
  selector: 'app-version-link',
  templateUrl: './version-link.component.html',
  styleUrls: ['./version-link.component.css']
})
export class VersionLinkComponent implements OnInit, OnDestroy {

  songLink: SongLink;
  private sub: Subscription;
  song1: Song;
  song2: Song;

  constructor(
    private activatedRoute: ActivatedRoute,
    private songLinkService: SongLinkDataService,
    private titleService: Title,
    private songService: SongService,
    public auth: AuthService,
    private router: Router,
    private dialog: MatDialog,
  ) {
    auth.getUserFromLocalStorage();
  }

  ngOnInit() {
    this.titleService.setTitle('SongLink');
    this.sub = this.activatedRoute.params.subscribe(params => {
      if (params['id']) {
        const id = params['id'];
        const role = this.auth.getUser().getRolePath();
        this.songLinkService.getSongLink(role, id).subscribe(
          (songLink) => {
            this.songLink = songLink;
            this.songService.getSong(songLink.songId1).subscribe((song) => {
              if (song != undefined) {
                this.song1 = new Song(song);
              }
            });
            this.songService.getSong(songLink.songId2).subscribe((song) => {
              if (song != undefined) {
                this.song2 = new Song(song);
              }
            });
          },
          (err) => {
            if (err.message === 'Unexpected token < in JSON at position 0') {
              this.openAuthenticateDialog();
            }
          });
      }
    });
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  private openAuthenticateDialog() {
    let user = JSON.parse(localStorage.getItem('currentUser'));
    const dialogRef = this.dialog.open(AuthenticateComponent, {
      data: {
        email: user.email
      }
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result === 'ok') {
        this.ngOnInit();
      }
    });
  }

  onRejectButtonClick() {
    this.uploadSongLinkAsApplied();
  }

  private uploadSongLinkAsApplied() {
    let songLink = new SongLink(this.songLink);
    songLink.applied = true;
    const role = this.auth.getUser().getRolePath();
    this.songLinkService.update(role, songLink).subscribe(
      () => {
        // noinspection JSIgnoredPromiseFromCall
        this.router.navigate(['/versionLinks']);
      },
      (err) => {
        if (err.status === 405) {
          this.openAuthenticateDialog();
        } else {
          console.log(err);
        }
      }
    );
  }

  onMergeSongsButtonClick() {
    const role = this.auth.getUser().getRolePath();
    this.songService.mergeVersionGroup(this.songLink.songId1, this.songLink.songId2, role).subscribe(
      (res) => {
        if (res.status === 202) {
          this.uploadSongLinkAsApplied();
        } else {
          console.log(res);
          this.openAuthenticateDialog();
        }
      },
      (err) => {
        if (err.message === 'Unexpected token < in JSON at position 0') {
          this.openAuthenticateDialog();
        } else {
          console.log(err);
        }
      }
    );
  }
}
