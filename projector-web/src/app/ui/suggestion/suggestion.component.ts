import {Component, OnDestroy, OnInit} from '@angular/core';
import {Subscription} from "rxjs/Subscription";
import {Song, SongService} from "../../services/song-service.service";
import {AuthService} from "../../services/auth.service";
import {ActivatedRoute} from "@angular/router";
import {Suggestion} from "../../models/suggestion";
import {SuggestionDataService} from "../../services/suggestion-data.service";
import {Title} from "@angular/platform-browser";

@Component({
  selector: 'app-suggestion',
  templateUrl: './suggestion.component.html',
  styleUrls: ['./suggestion.component.css']
})
export class SuggestionComponent implements OnInit, OnDestroy {

  song: Song;
  suggestionSong: Song;
  suggestion: Suggestion;
  private sub: Subscription;

  constructor(private activatedRoute: ActivatedRoute,
              private suggestionService: SuggestionDataService,
              private titleService: Title,
              private songService: SongService,
              public auth: AuthService) {
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


}
