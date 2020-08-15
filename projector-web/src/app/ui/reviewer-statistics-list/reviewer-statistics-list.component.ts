import { Component, OnInit, Input } from '@angular/core';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { Observable } from 'rxjs/Observable';
import { DataSource } from '@angular/cdk/table';
import { User } from '../../models/user';
import { SongService, Song } from '../../services/song-service.service';
import { PageEvent } from '@angular/material';
import { FormControl } from '@angular/forms';
import { SongListComponent } from '../song-list/song-list.component';

class ReviewerStatistics {
  nr: number;
  song: Song;
}

export class StatisticsDatabase {
  dataChange: BehaviorSubject<ReviewerStatistics[]> = new BehaviorSubject<ReviewerStatistics[]>([]);

  constructor(reviewerStatisticsList: ReviewerStatistics[]) {
    if (reviewerStatisticsList !== null) {
      const copiedData = this.data;
      let nr = 0;
      for (const reviewerStatistics of reviewerStatisticsList) {
        reviewerStatistics.nr = ++nr;
        copiedData.push(reviewerStatistics);
        this.dataChange.next(copiedData);
      }

      this.dataChange.next(copiedData);
    }
  }

  get data(): ReviewerStatistics[] {
    return this.dataChange.value;
  }
}

export class StatisticsDataSource extends DataSource<any> {
  constructor(private _statisticsDatabase: StatisticsDatabase) {
    super();
  }

  connect(): Observable<ReviewerStatistics[]> {
    return this._statisticsDatabase.dataChange;
  }

  disconnect() {
  }
}

@Component({
  selector: 'app-reviewer-statistics-list',
  templateUrl: './reviewer-statistics-list.component.html',
  styleUrls: ['./reviewer-statistics-list.component.css']
})
export class ReviewerStatisticsListComponent implements OnInit {

  displayedColumns = ['Nr', 'reviewedDate', 'title'];
  @Input()
  user: User;
  dataSource: StatisticsDataSource | null;
  songControl: FormControl;
  pageE: PageEvent;
  songTitles: Song[];
  filteredSongs: Observable<Song[]>;
  filteredSongsList: Song[];
  paginatedSongs: Song[];

  constructor(
    private songService: SongService,
  ) {
    this.songControl = new FormControl();
    this.filteredSongsList = [];
    this.paginatedSongs = [];
  }

  ngOnInit() {
    this.filteredSongs = this.songControl.valueChanges
      .startWith(null)
      .map(song => song ? this.filterStates(song) : this.songTitles.slice());
    const pageEvent = new PageEvent();
    pageEvent.pageSize = JSON.parse(sessionStorage.getItem("pageSize2"));
    pageEvent.pageIndex = JSON.parse(sessionStorage.getItem("pageIndex2"));
    if (pageEvent.pageSize == undefined) {
      pageEvent.pageSize = 10;
    }
    if (pageEvent.pageIndex == undefined) {
      pageEvent.pageIndex = 0;
    }
    this.pageE = pageEvent;
    this.getSongTitles();
  }

  private getSongTitles() {
    this.songService.getAllSongTitlesReviewedByUser(this.user).subscribe((songs) => {
      this.fillData(songs);
    });
  }

  private fillData(songs: Song[]) {
    const reviewerStatistics: ReviewerStatistics[] = [];
    for (const song of songs) {
      const reviewerStatistic = new ReviewerStatistics();
      reviewerStatistic.song = song;
      reviewerStatistics.push(reviewerStatistic);
    }
    const database = new StatisticsDatabase(reviewerStatistics);
    this.dataSource = new StatisticsDataSource(database);
    this.songControl.updateValueAndValidity();
  }

  pageEvent(pageEvent: PageEvent) {
    this.pageE = pageEvent;
    const start = this.pageE.pageIndex * this.pageE.pageSize;
    sessionStorage.setItem("pageIndex2", JSON.stringify(this.pageE.pageIndex));
    sessionStorage.setItem("pageSize2", JSON.stringify(this.pageE.pageSize));
    const end = (this.pageE.pageIndex + 1) * this.pageE.pageSize;
    this.paginatedSongs = this.filteredSongsList.slice(start, end);
    this.fillData(this.paginatedSongs);
  }

  filterStates(filter: string) {
    filter = SongListComponent.stripAccents(filter);
    return this.songTitles.filter(song => {
      return SongListComponent.stripAccents(song.title).indexOf(filter) >= 0;
    }
    );
  }

}
