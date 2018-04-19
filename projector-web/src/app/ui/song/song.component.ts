import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Song, SongService} from '../../services/song-service.service';
import {Subscription} from 'rxjs/Subscription';
import {AuthService} from '../../services/auth.service';

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
  private sub: Subscription;

  constructor(private activatedRoute: ActivatedRoute,
              private songService: SongService,
              public auth: AuthService) {
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
    this.songsByVersionGroup = [];
    this.loadVersionGroup();
    history.replaceState('data to be passed', this.song.title, window.location.href.replace('/#/song/', '/song/'));
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
          history.replaceState('data to be passed', this.song.title, window.location.href.replace('/#/song/', '/song/'));
          this.originalSong = new Song(song);
          if (song.originalId !== undefined) {
            this.songService.getSong(song.originalId).subscribe((song) => {
              this.secondSong = song;
            });
          }
          this.loadVersionGroup();
        });
      }
    });
  }

  loadVersionGroup() {
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
    this.songService.deleteById(this.song.uuid).subscribe(() => {

    });
  }

  eraseSong() {
    this.songService.eraseById(this.song.uuid).subscribe(() => {
    });
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
    this.songService.updateSong(updateSong).subscribe(
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
    this.songService.mergeVersionGroup(this.song.uuid, this.markedVersionGroup).subscribe(
      () => {
        this.markedVersionGroup = null;
      },
      (err) => {
        console.log(err);
      }
    );
  }
}
