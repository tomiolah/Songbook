import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Song, SongService } from '../../services/song-service.service';
import { Subscription } from 'rxjs/Subscription';
import { AuthService } from '../../services/auth.service';
import { DomSanitizer, SafeResourceUrl, Title } from "@angular/platform-browser";
import { MatDialog, MatSnackBar } from "@angular/material";
import { ShareComponent } from "../share/share.component";
import { AuthenticateComponent } from "../authenticate/authenticate.component";
import { OpenInAppComponent } from "../open-in-app/open-in-app.component";
import { AddToCollectionComponent } from "../add-to-collection/add-to-collection.component";
import { MobileOsTypeEnum } from "../../util/enums";

@Component({
  selector: 'app-song',
  templateUrl: './song.component.html',
  styleUrls: ['./song.component.css']
})
export class SongComponent implements OnInit, OnDestroy {
  song: Song;
  originalSong: Song;
  editing = false;
  showSimilarities = false;
  similar: Song[];
  secondSong: Song;
  receivedSimilar = false;
  markText = "Mark for version group";
  marked = false;
  markedVersionGroup: string;
  songsByVersionGroup: Song[] = [];
  public safeUrl: SafeResourceUrl = null;
  public isAndroid = false;
  private sub: Subscription;
  eraseSongType = 1;
  mergeVersionGroupType = 2;
  public isIos = false;

  constructor(private activatedRoute: ActivatedRoute,
    private songService: SongService,
    private dialog: MatDialog,
    public auth: AuthService,
    private titleService: Title,
    private snackBar: MatSnackBar,
    private router: Router,
    public sanitizer: DomSanitizer) {
    auth.getUserFromLocalStorage();
    this.markedVersionGroup = localStorage.getItem("markedVersionGroup");
    if (this.markedVersionGroup == 'null') {
      this.markedVersionGroup = null;
    }
  }

  @Input()
  set i_song(song: Song) {
    for (const songVerse of song.songVerseDTOS) {
      songVerse.lines = [];
      for (const s of songVerse.text.split('\n')) {
        songVerse.lines.push(s);
      }
    }
    if (song.originalId !== undefined) {
      this.songService.getSong(song.originalId).subscribe((song) => {
        this.secondSong = song;
      });
    }
    this.originalSong = new Song(song);
    this.song = song;
    this.calculateUrlId(song.youtubeUrl);
    this.titleService.setTitle(this.song.title);
    this.songsByVersionGroup = [];
    this.loadVersionGroup();
    if (!song.deleted) {
      history.replaceState('data to be passed', this.song.title, window.location.href.replace('/#/song/', '/song/'));
    }
  }

  // noinspection JSMethodCanBeStatic
  public openInNewTab(song: Song) {
    window.open('/#/song/' + song.uuid);
  }

  ngOnInit() {
    this.secondSong = null;
    if (this.song === undefined) {
      this.song = new Song();
      this.song.title = 'Loading';
      this.song.songVerseDTOS = [];
    }
    this.sub = this.activatedRoute.params.subscribe(params => {
      if (params['id']) {
        const songId = params['id'];
        this.songService.getSong(songId).subscribe((song) => {
          for (const songVerse of song.songVerseDTOS) {
            songVerse.lines = [];
            for (const s of songVerse.text.split('\n')) {
              songVerse.lines.push(s);
            }
          }
          this.song = song;
          this.calculateUrlId(song.youtubeUrl);
          this.titleService.setTitle(this.song.title);
          if (!song.deleted) {
            (<any>window).ga('set', 'page', "/song/" + songId);
            (<any>window).ga('send', 'pageview');
            history.replaceState('data to be passed', this.song.title, window.location.href.replace('/#/song/', '/song/'));
          }
          this.originalSong = new Song(song);
          if (song.originalId !== undefined) {
            this.songService.getSong(song.originalId).subscribe((song) => {
              this.secondSong = song;
            });
          }
          this.isAndroid = /(android)/i.test(navigator.userAgent);
          this.isIos = /iPad|iPhone|iPod/i.test(navigator.userAgent);
          if (this.isAndroid) {
            this.showOpenInAppDialog(MobileOsTypeEnum.Android);
          } else if (this.isIos) {
            this.showOpenInAppDialog(MobileOsTypeEnum.Ios);
          }
          this.loadVersionGroup();
          const user = this.auth.getUser();
          if (this.auth.isLoggedIn && user != undefined && (user.hasReviewerRoleForSong(this.song) || user.isAdmin())) {
            this.showSimilar();
          }
        });
      }
    });
  }

  loadVersionGroup() {
    this.songsByVersionGroup = [];
    let id = this.song.versionGroup;
    if (id == null && this.song.uuid != undefined) {
      id = this.song.uuid;
    }
    this.songService.getSongsByVersionGroup(id).subscribe((songs) => {
      for (const song of songs) {
        if (song.uuid != this.song.uuid) {
          this.songsByVersionGroup.push(song);
        }
      }
    });

  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  deleteSong() {
    const role = this.auth.getUser().getRolePath();
    this.songService.deleteById(role, this.song.uuid).subscribe(() => {

    });
  }

  eraseSong() {
    const role = this.auth.getUser().getRolePath();
    this.songService.eraseById(role, this.song.uuid).subscribe(
      () => {
        // noinspection JSIgnoredPromiseFromCall
        this.router.navigate(['/songs']);
      },
      (err) => {
        if (err.message === 'Unexpected token < in JSON at position 0') {
          this.openAuthenticateDialog(this.eraseSongType);
        } else {
          console.log(err);
          this.snackBar.open(err._body, 'Close', {
            duration: 5000
          })
        }
      }
    );
  }

  publishSong() {
    this.songService.publishById(this.song.uuid).subscribe(() => {

    });
  }

  editSong() {
    this.editing = true;
  }

  showSimilar() {
    this.similar = [];
    this.receivedSimilar = false;
    this.songService.getSimilar(this.song).subscribe((songs) => {
      this.similar = songs;
      if (songs.length > 0) {
        this.secondSong = this.similar[0];
      }
      this.receivedSimilar = true;
    });
    this.showSimilarities = true;
  }

  selectSecondSong(song: Song) {
    this.secondSong = song;
  }

  acceptChanges() {
    let updateSong = new Song();
    let uuid = this.secondSong.uuid;
    let id = this.secondSong.id;
    Object.assign(updateSong, this.originalSong);
    updateSong.uuid = uuid;
    updateSong.id = id;
    updateSong.modifiedDate = this.secondSong.modifiedDate;
    updateSong.deleted = false;
    const role = this.auth.getUser().getRolePath();
    this.songService.updateSong(role, updateSong).subscribe(
      () => {
      },
      (err) => {
        console.log(err);
      }
    );
  }

  markForVersionGroup() {
    this.markedVersionGroup = null;
    this.marked = !this.marked;
    if (this.marked) {
      this.markText = "Remove mark for version group";
      let versionGroup = this.song.uuid;
      if (this.song.versionGroup != null) {
        versionGroup = this.song.versionGroup;
      }
      localStorage.setItem("markedVersionGroup", versionGroup);
    } else {
      this.markText = "Mark for version group";
      localStorage.setItem("markedVersionGroup", null);
    }
  }

  mergeVersionGroup() {
    this.markedVersionGroup = localStorage.getItem("markedVersionGroup");
    if (this.markedVersionGroup == 'null') {
      this.markedVersionGroup = null;
      return;
    }
    const user = this.auth.getUser().isAdmin() ? 'admin' : 'user';
    this.songService.mergeVersionGroup(this.song.uuid, this.markedVersionGroup, user).subscribe(
      (res) => {
        if (res.status === 202) {
          this.markedVersionGroup = null;
        } else {
          console.log(res);
          this.openAuthenticateDialog(this.mergeVersionGroupType);
        }
      },
      (err) => {
        if (err.message === 'Unexpected token < in JSON at position 0') {
          this.openAuthenticateDialog(this.mergeVersionGroupType);
        } else {
          console.log(err);
          this.snackBar.open(err._body, 'Close', {
            duration: 5000
          })
        }
      }
    );
  }

  openShareDialog(): void {
    const config = {
      data: {
        uuid: this.song.uuid,
        title: this.song.title,
      }
    };
    const dialogRef = this.dialog.open(ShareComponent, config);

    dialogRef.afterClosed().subscribe(() => {
    });
  }

  showOpenInAppDialog(mobileOsType: MobileOsTypeEnum): void {
    if (localStorage.getItem("OpenInAppComponent_dontShow") != undefined) {
      return;
    }
    localStorage.setItem("mobileOsType", MobileOsTypeEnum[mobileOsType]);
    const config = {
      data: {
        uuid: this.song.uuid,
        title: this.song.title,
      }
    };
    const dialogRef = this.dialog.open(OpenInAppComponent, config);

    dialogRef.afterClosed().subscribe(() => {
    });
  }

  calculateUrlId(url: string) {
    if (url == undefined) {
      this.safeUrl = null;
      return;
    }
    let youtubeUrl = url.replace("https://www.youtube.com/watch?v=", "");
    youtubeUrl = youtubeUrl.replace("https://www.youtube.com/embed/", "");
    youtubeUrl = youtubeUrl.replace("https://youtu.be/", "");
    let indexOf = youtubeUrl.indexOf('?');
    if (indexOf >= 0) {
      youtubeUrl = youtubeUrl.substring(0, indexOf);
    }
    if (youtubeUrl.length < 21 && youtubeUrl.length > 9) {
      this.safeUrl = this.sanitizer.bypassSecurityTrustResourceUrl("https://www.youtube.com/embed/" + youtubeUrl);
    } else {
      this.safeUrl = null;
    }
  }

  addToCollectionSong() {
    this.openAddToCollectionDialog();
  }

  openAddToCollectionDialog(): void {
    const dialogRef = this.dialog.open(AddToCollectionComponent, {
      data: {
        song: this.song
      }
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result === 'ok') {
      }
    });
  }

  private openAuthenticateDialog(type: number) {
    let user = JSON.parse(localStorage.getItem('currentUser'));
    const dialogRef = this.dialog.open(AuthenticateComponent, {
      data: {
        email: user.email
      }
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result === 'ok') {
        if (type == this.mergeVersionGroupType) {
          this.mergeVersionGroup();
        } else if (type == this.eraseSongType) {
          this.eraseSong();
        }
      }
    });
  }
}
