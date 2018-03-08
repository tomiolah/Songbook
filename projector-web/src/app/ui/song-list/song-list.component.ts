import {Component, OnInit} from '@angular/core';
import {Song, SongService} from '../../services/song-service.service';
import {FormControl} from '@angular/forms';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/startWith';
import 'rxjs/add/operator/map';
import {PageEvent} from '@angular/material/paginator';
import {Router} from '@angular/router';
import {AuthService} from "../../services/auth.service";

@Component({
  selector: 'app-song-list',
  templateUrl: './song-list.component.html',
  styleUrls: ['./song-list.component.css']
})
export class SongListComponent implements OnInit {

  filteredSongsList: Song[];
  songTitles: Song[];
  songControl: FormControl;
  filteredSongs: Observable<Song[]>;
  song: Song;
  pageE: PageEvent;
  paginatedSongs: Song[];
  isSortByModifiedDate = true;
  songTitlesLocalStorage: Song[];
  isShowUploaded = false;
  private songListComponent_sortByModifiedDate = 'songListComponent_sortByModifiedDate';
  private songListComponent_showUploaded = 'songListComponent_showUploaded';

  constructor(private songServiceService: SongService,
              private router: Router,
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
  }

  ngOnInit() {
    this.isSortByModifiedDate = JSON.parse(localStorage.getItem(this.songListComponent_sortByModifiedDate));
    this.isShowUploaded = JSON.parse(localStorage.getItem(this.songListComponent_showUploaded));
    this.loadSongs();

    this.songControl.valueChanges.subscribe(value => {
      for (const song of this.songTitles) {
        if (song.title === value) {
          this.song = song;
          return;
        }
      }
    });
    this.filteredSongs.subscribe(filteredSongsList => {
        const start = this.pageE.pageIndex * this.pageE.pageSize;
        const end = (this.pageE.pageIndex + 1) * this.pageE.pageSize;
        this.paginatedSongs = filteredSongsList.slice(start, end);
        this.filteredSongsList = filteredSongsList;
      }
    );
  }

  filterStates(filter: string) {
    return this.songTitles.filter(song => {
        return song.title.toLowerCase().indexOf(filter.toLowerCase()) >= 0;
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
    localStorage.setItem(this.songListComponent_sortByModifiedDate, JSON.stringify(this.isSortByModifiedDate));
    this.sortSongTitles();
    this.filteredSongs = this.songControl.valueChanges
      .startWith(null)
      .map(song => song ? this.filterStates(song) : this.songTitles.slice());
    this.songControl.updateValueAndValidity();
  }

  changeShowUploaded() {
    localStorage.setItem(this.songListComponent_showUploaded, JSON.stringify(this.isShowUploaded));
    this.loadSongs();
  }

  private loadSongs() {
    if (!this.isShowUploaded) {
      this.songServiceService.getAllSongTitlesAfterModifiedDate(this.songTitles[0].modifiedDate).subscribe(
        (songTitles) => {
          for (const song of songTitles) {
            if (song.deleted) {
              this.removeSong(song);
              const index = songTitles.indexOf(song, 0);
              if (index > -1) {
                songTitles.splice(index, 1);
              }
            } else {
              const index = this.containsInLocalStorage(song);
              if (index > -1) {
                this.songTitlesLocalStorage.splice(index, 1);
              }
            }
          }
          this.songTitles = this.songTitlesLocalStorage.concat(songTitles);
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
          localStorage.setItem('songTitles', JSON.stringify(this.songTitles));
        }
      );
    } else {
      this.songServiceService.getAllUploadedSongTitles().subscribe(
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
    }
  }

  private containsInLocalStorage(song) {
    let index = 0;
    for (const songTitle of this.songTitlesLocalStorage) {
      if (songTitle.id === song.id) {
        return index;
      }
      ++index;
    }
    return -1;
  }

  private removeSong(song) {
    const index = this.getIndex(song);
    if (index > -1) {
      this.songTitlesLocalStorage.splice(index, 1);
    }
    return index;
  }

  private getIndex(searchedSong) {
    let index = -1;
    let i = 0;
    for (const song of this.songTitlesLocalStorage) {
      if (song.id != null && song.id === searchedSong.id) {
        index = i;
        break;
      }
      ++i;
    }
    return index;
  }

  private sortSongTitles() {
    if (this.isSortByModifiedDate) {
      this.sortSongTitlesByModifiedDate();
    } else {
      this.songTitles.sort((song1, song2) => {
        if (song1.title.toLocaleLowerCase() > song2.title.toLocaleLowerCase()) {
          return 1;
        }
        if (song1.title.toLocaleLowerCase() < song2.title.toLocaleLowerCase()) {
          return -1;
        }
        return 0;
      });
    }
  }

  private sortSongTitlesByModifiedDate() {
    this.songTitles.sort((song1, song2) => {
      if (song1.modifiedDate < song2.modifiedDate) {
        return 1;
      }
      if (song1.modifiedDate > song2.modifiedDate) {
        return -1;
      }
      return 0;
    });
  }
}
