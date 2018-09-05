import {Component, OnDestroy, OnInit} from '@angular/core';
import {Subscription} from "rxjs/Subscription";
import {Song, SongService} from "../../services/song-service.service";
import {AuthService} from "../../services/auth.service";
import {ActivatedRoute} from "@angular/router";
import {Suggestion} from "../../models/suggestion";
import {SuggestionDataService} from "../../services/suggestion-data.service";
import {DomSanitizer, SafeResourceUrl, Title} from "@angular/platform-browser";

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
              public sanitizer: DomSanitizer) {
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
        this.suggestionService.getSuggestion(suggestionId).subscribe((suggestion) => {
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
        });
      }
    });
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
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

}
