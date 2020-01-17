import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from "rxjs/Subscription";
import { Song, SongService } from "../../services/song-service.service";
import { AuthService } from "../../services/auth.service";
import { ActivatedRoute, Router } from "@angular/router";
import { Suggestion } from "../../models/suggestion";
import { SuggestionDataService } from "../../services/suggestion-data.service";
import { DomSanitizer, SafeResourceUrl, Title } from "@angular/platform-browser";
import { AuthenticateComponent } from "../authenticate/authenticate.component";
import { MatDialog } from "@angular/material";

@Component({
  selector: 'app-suggestion',
  templateUrl: './suggestion.component.html',
  styleUrls: ['./suggestion.component.css']
})
export class SuggestionComponent implements OnInit, OnDestroy {

  song: Song;
  suggestionSong: Song;
  suggestion: Suggestion;
  public safeUrl: SafeResourceUrl = null;
  private sub: Subscription;

  constructor(private activatedRoute: ActivatedRoute,
    private suggestionService: SuggestionDataService,
    private titleService: Title,
    private songService: SongService,
    public auth: AuthService,
    public sanitizer: DomSanitizer,
    private router: Router,
    private dialog: MatDialog) {
    auth.getUserFromLocalStorage();
  }

  ngOnInit() {
    this.titleService.setTitle('Suggestion');
    this.song = new Song();
    this.song.title = 'Loading';
    this.song.songVerseDTOS = [];
    this.suggestion = new Suggestion();
    this.suggestion.title = "Loading";
    this.suggestionSong = new Song();
    this.suggestionSong.songVerseDTOS = [];
    this.sub = this.activatedRoute.params.subscribe(params => {
      if (params['suggestionId']) {
        const suggestionId = params['suggestionId'];
        const role = this.auth.getUser().getRolePath();
        this.suggestionService.getSuggestion(role, suggestionId).subscribe(
          (suggestion) => {
            this.suggestion = suggestion;
            if (this.suggestion.title != undefined) {
              this.suggestionSong = new Song();
              this.suggestionSong.title = this.suggestion.title;
              this.suggestionSong.songVerseDTOS = this.suggestion.verses;
            } else {
              this.suggestionSong = undefined;
            }
            if (this.suggestion.youtubeUrl != undefined) {
              this.calculateUrlId(this.suggestion.youtubeUrl);
            }
            this.songService.getSong(suggestion.songId).subscribe((song) => {
              this.song = song;
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

  onDoneButtonClick() {
    let suggestion = new Suggestion(this.suggestion);
    suggestion.reviewed = true;
    const role = this.auth.getUser().getRolePath();
    this.suggestionService.update(role, this.suggestion).subscribe(
      () => {
        // noinspection JSIgnoredPromiseFromCall
        this.router.navigate(['/suggestions']);
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

}
