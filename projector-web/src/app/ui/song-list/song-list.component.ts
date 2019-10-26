import { Component, OnInit } from '@angular/core';
import { Song, SongService } from '../../services/song-service.service';
import { FormControl } from '@angular/forms';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/startWith';
import 'rxjs/add/operator/map';
import { PageEvent } from '@angular/material/paginator';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { AuthService } from "../../services/auth.service";
import { Language } from "../../models/language";
import { LanguageDataService } from "../../services/language-data.service";
import { Subscription } from "rxjs/Subscription";
import { Title } from "@angular/platform-browser";
import { User } from '../../models/user';

@Component({
  selector: 'app-song-list',
  templateUrl: './song-list.component.html',
  styleUrls: ['./song-list.component.css']
})
export class SongListComponent implements OnInit {
  sortMode = "sortMode";
  filteredSongsList: Song[];
  songTitles: Song[];
  songControl: FormControl;
  filteredSongs: Observable<Song[]>;
  song: Song;
  pageE: PageEvent;
  paginatedSongs: Song[];
  sortType = "RELEVANCE";
  songTitlesLocalStorage: Song[];
  songsType = Song.PUBLIC;
  languages: Language[];
  selectedLanguage: Language;
  languagesKey = 'languages_v2';
  private songListComponent_songsType = 'songListComponent_songsType';
  private _subscription: Subscription;

  constructor(private songService: SongService,
    private router: Router,
    private languageDataService: LanguageDataService,
    private titleService: Title,
    private activatedRoute: ActivatedRoute,
    public auth: AuthService) {
    this.songControl = new FormControl();
    this.songTitles = [];
    this.paginatedSongs = [];
    this.filteredSongsList = [];
    this.songTitlesLocalStorage = JSON.parse(localStorage.getItem('songTitles'));
    if (this.songTitlesLocalStorage) {
      this.songTitles = this.songTitlesLocalStorage;
      this.sortSongTitles();
    } else {
      this.songTitlesLocalStorage = [];
      const song1 = new Song();
      song1.title = 'loading';
      song1.modifiedDate = new Date(0).getMilliseconds();
      this.songTitles.push(song1);
    }
    this.filteredSongs = this.songControl.valueChanges
      .startWith(null)
      .map(song => song ? this.filterStates(song) : this.songTitles.slice());
    let pageEvent = new PageEvent();
    pageEvent.pageSize = JSON.parse(sessionStorage.getItem("pageSize"));
    pageEvent.pageIndex = JSON.parse(sessionStorage.getItem("pageIndex"));
    if (pageEvent.pageSize == undefined) {
      pageEvent.pageSize = 10;
    }
    if (pageEvent.pageIndex == undefined) {
      pageEvent.pageIndex = 0;
    }
    this.pageE = pageEvent;
    this.languages = [];
  }

  ngOnInit() {
    this.titleService.setTitle('Songs');
    this.sortType = JSON.parse(localStorage.getItem(this.sortMode));
    if (this.sortType === null) {
      this.sortType = "RELEVANCE";
    }
    this.songsType = localStorage.getItem(this.songListComponent_songsType);
    if (this.songsType === null) {
      this.songsType = Song.PUBLIC;
    }
    this.songControl.valueChanges.subscribe(value => {
      for (const song of this.songTitles) {
        if (song.title === value) {
          this.song = song;
          return;
        }
      }
    });
    this.filteredSongs.subscribe(filteredSongsList => {
      let pageIndex = JSON.parse(sessionStorage.getItem("pageIndex"));
      let start = pageIndex * this.pageE.pageSize;
      while (start > filteredSongsList.length) {
        pageIndex -= 1;
        start = pageIndex * this.pageE.pageSize;
      }
      this.pageE.pageIndex = pageIndex;
      const end = (pageIndex + 1) * this.pageE.pageSize;
      this.paginatedSongs = filteredSongsList.slice(start, end);
      this.filteredSongsList = filteredSongsList;
    }
    );
    this.loadLanguage();
  }

  loadLanguage() {
    this.languageDataService.getAll().subscribe(
      (languages) => {
        this.languages = languages;
        if (localStorage.getItem(this.languagesKey) == null) {
          localStorage.setItem(this.languagesKey, JSON.stringify(languages));
        } else {
          let localStorageLanguages: Language[] = JSON.parse(localStorage.getItem(this.languagesKey));
          if (localStorageLanguages.length != languages.length) {
            let index = 0;
            for (let storageLanguage of localStorageLanguages) {
              let was = false;
              for (let language of languages) {
                if (language.uuid == storageLanguage.uuid) {
                  was = true;
                  break;
                }
              }
              if (!was) {
                localStorageLanguages.splice(index, 0);
              } else {
                ++index;
              }
            }
            for (let language of languages) {
              let was = false;
              for (let storageLanguage of localStorageLanguages) {
                if (language.uuid == storageLanguage.uuid) {
                  was = true;
                  break;
                }
              }
              if (!was) {
                localStorageLanguages = localStorageLanguages.concat(language);
              }
            }
            localStorage.setItem(this.languagesKey, JSON.stringify(localStorageLanguages));
          }
        }
        this.selectedLanguage = JSON.parse(localStorage.getItem('selectedLanguage'));
        if (this.selectedLanguage == null) {
          this.selectedLanguage = languages[0];
          localStorage.setItem('selectedLanguage', JSON.stringify(this.selectedLanguage));
        } else {
          for (let language of languages) {
            if (language.uuid == this.selectedLanguage.uuid) {
              this.selectedLanguage = language;
              break;
            }
          }
        }
        this.loadSongs();
      }
    );
  }

  stripAccents(s) {
    s = s.normalize('NFD').replace(/[\u0300-\u036f]/g, "");
    s = s.replace(/[^a-zA-Z]/g, '');
    return s.toLowerCase();
  }

  filterStates(filter: string) {
    filter = this.stripAccents(filter);
    return this.songTitles.filter(song => {
      return this.stripAccents(song.title).indexOf(filter) >= 0;
    }
    );
  }

  selectSong(selectedSong: Song) {
    if (selectedSong != null && selectedSong.id != null) {
      // noinspection JSIgnoredPromiseFromCall
      this.router.navigate(['/song/', selectedSong.id]);
    }
  }

  pageEvent(pageEvent: PageEvent) {
    this.pageE = pageEvent;
    const start = this.pageE.pageIndex * this.pageE.pageSize;
    sessionStorage.setItem("pageIndex", JSON.stringify(this.pageE.pageIndex));
    sessionStorage.setItem("pageSize", JSON.stringify(this.pageE.pageSize));
    const end = (this.pageE.pageIndex + 1) * this.pageE.pageSize;
    this.paginatedSongs = this.filteredSongsList.slice(start, end);
  }

  changeSorting() {
    localStorage.setItem(this.sortMode, JSON.stringify(this.sortType));
    this.sortSongTitles();
    this.filteredSongs = this.songControl.valueChanges
      .startWith(null)
      .map(song => song ? this.filterStates(song) : this.songTitles.slice());
    this.songControl.updateValueAndValidity();
  }

  songsTypeChange() {
    localStorage.setItem(this.songListComponent_songsType, this.songsType);
    this.loadSongs();
  }

  selectLanguage(language: Language) {
    localStorage.setItem('selectedLanguage', JSON.stringify(this.selectedLanguage));
    if (this._subscription != undefined) {
      this._subscription.unsubscribe();
    }
    let languages: Language[] = JSON.parse(localStorage.getItem(this.languagesKey));
    for (let lang of languages) {
      if (lang.uuid === language.uuid) {
        if (lang.songTitles === undefined) {
          lang.songTitles = [];
        } else {
          this.songTitles = lang.songTitles;
          this.sortSongTitles();
          this.songControl.updateValueAndValidity();
        }
        let modifiedDate = 0;
        for (let song of lang.songTitles) {
          if (modifiedDate < song.modifiedDate) {
            modifiedDate = song.modifiedDate;
          }
        }
        this._subscription = this.songService.getAllSongTitlesAfterModifiedDate(modifiedDate, language.uuid).subscribe(songTitles => {
          if (lang.songTitles.length == 0) {
            this.songTitles = songTitles;
            for (let song of songTitles) {
              if (song.deleted) {
                this.removeSong(song, this.songTitles);
              }
            }
            lang.songTitles = this.songTitles;
          } else {
            for (const song of songTitles) {
              if (song.deleted) {
                this.removeSong(song, lang.songTitles);
                const index = songTitles.indexOf(song, 0);
                if (index > -1) {
                  songTitles.splice(index, 1);
                }
              } else {
                const index = this.containsInLocalStorage(song, lang.songTitles);
                if (index > -1) {
                  lang.songTitles.splice(index, 1);
                }
              }
            }
            this.songTitles = lang.songTitles.concat(songTitles);
            lang.songTitles = this.songTitles;
          }
          localStorage.setItem(this.languagesKey, JSON.stringify(languages));
          this.sortAndUpdate();
        });
      }
    }
  }

  allLanguages() {
    if (this._subscription != undefined) {
      this._subscription.unsubscribe();
    }
    let languages: Language[] = JSON.parse(localStorage.getItem(this.languagesKey));
    this.songTitles = [];
    for (let language of languages) {
      this.songTitles = this.songTitles.concat(language.songTitles);
    }
    this.sortAndUpdate();
  }

  searchTermTyped() {
    const params: Params = Object.assign({}, this.activatedRoute.snapshot.queryParams);
    params['search'] = this.songControl.value;
    params['language'] = this.selectedLanguage.uuid;
    this.router.navigate(['.'], { queryParams: params });
  }

  private sortAndUpdate() {
    this.sortSongTitles();
    this.songControl.updateValueAndValidity();
    const pageEvent = new PageEvent();
    pageEvent.pageSize = JSON.parse(sessionStorage.getItem("pageSize"));
    pageEvent.pageIndex = JSON.parse(sessionStorage.getItem("pageIndex"));
    if (pageEvent.pageSize == undefined) {
      pageEvent.pageSize = 10;
    }
    if (pageEvent.pageIndex == undefined) {
      pageEvent.pageIndex = 0;
    }
    this.pageEvent(pageEvent);
  }

  private loadSongs() {
    switch (this.songsType) {
      case Song.PUBLIC:
        this.selectLanguage(this.selectedLanguage);
        break;
      case Song.UPLOADED:
        this.songService.getAllUploadedSongTitles().subscribe(
          (songTitles) => {
            this.songTitles = songTitles;
            this.sortSongTitles();
            this.songControl.updateValueAndValidity();
            const pageEvent = new PageEvent();
            pageEvent.pageSize = JSON.parse(sessionStorage.getItem("pageSize"));
            pageEvent.pageIndex = JSON.parse(sessionStorage.getItem("pageIndex"));
            if (pageEvent.pageSize == undefined) {
              pageEvent.pageSize = 10;
            }
            if (pageEvent.pageIndex == undefined) {
              pageEvent.pageIndex = 0;
            }
            this.pageEvent(pageEvent);
          }
        );
        break;
      case Song.REVIEWER:
        this.selectLanguage(this.selectedLanguage);
        break;
    }
    this.activatedRoute.queryParams.subscribe((queryParams) => {
      let search = queryParams['search'];
      let language = queryParams['language'];
      for (let l of this.languages) {
        if (l.uuid == language) {
          language = l;
          if (this.selectedLanguage != language) {
            this.selectedLanguage = language;
            this.selectLanguage(language);
          }
          break;
        }
      }
      this.songControl.patchValue(search);
    });
  }

  private containsInLocalStorage(song, titlesLocalStorage = this.songTitlesLocalStorage) {
    let index = 0;
    for (const songTitle of titlesLocalStorage) {
      if (songTitle.id === song.id) {
        return index;
      }
      ++index;
    }
    return -1;
  }

  private removeSong(song, songs = this.songTitlesLocalStorage) {
    const index = this.getIndex(song, songs);
    if (index > -1) {
      songs.splice(index, 1);
    }
    return index;
  }

  private getIndex(searchedSong, songs) {
    let index = -1;
    let i = 0;
    for (const song of songs) {
      if (song.id != null && song.id === searchedSong.id) {
        index = i;
        break;
      }
      ++i;
    }
    return index;
  }

  private sortSongTitles() {
    if (this.sortType === "MODIFIED_DATE") {
      this.sortSongTitlesByModifiedDate();
    } else if (this.sortType === "VIEWS") {
      this.songTitles.sort((song1, song2) => {
        if (song1.views < song2.views) {
          return 1;
        }
        if (song1.views > song2.views) {
          return -1;
        }
        return 0;
      });
    } else if (this.sortType === "RELEVANCE") {
      this.songTitles.sort((song1, song2) => {
        let score1 = Song.getScore(song1);
        let score2 = Song.getScore(song2);
        if (score1 < score2) {
          return 1;
        }
        if (score1 > score2) {
          return -1;
        }
        return this.compare(song2.modifiedDate, song1.modifiedDate);
      });
    } else {
      this.songTitles.sort((song1, song2) => {
        return this.compare(song1.title.toLocaleLowerCase(), song2.title.toLocaleLowerCase());
      });
    }
  }

  private compare(a, b) {
    if (a < b) {
      return -1;
    }
    if (a > b) {
      return 1;
    }
    return 0;
  }

  private sortSongTitlesByModifiedDate() {
    this.songTitles.sort((song1, song2) => {
      return this.compare(song2.modifiedDate, song1.modifiedDate);
    });
  }

  hasRoleForSongReview() {
    const user: User = this.auth.getUser();
    return this.auth.isLoggedIn && user.isReviewer();
  }
}

