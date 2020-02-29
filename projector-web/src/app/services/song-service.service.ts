import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Observable } from 'rxjs/Observable';
import { BaseModel } from '../models/base-model';
import { Language } from "../models/language";

export class ColorText {
  text: string;
  color = false;
  commonCount: number = 0;
  forwardIndexK: number;
  backwardIndexK: number;
  backwardColor = false;
}

export class WordCompare {
  text: string;
  color = false;
  characters: ColorText[] = [];
  commonCount: number = 0;
}

export class LineWord {
  words: WordCompare[] = [];
  modified = false;
}

export class LineCompare {
  text: string;
  color = false;
  lineWord: LineWord = new LineWord();
  commonCount: number = 0;
}

export enum SectionType {
  Intro, Verse, Pre_chorus, Chorus, Bridge, Coda
}

export class SongVerseDTO {
  lines: string[];
  lineCompareLines: LineCompare[];
  text = '';
  type: SectionType;
  was: boolean;

  constructor(values: Object = {}) {
    Object.assign(this, values);
  }

  chorus(): boolean {
    return this.type == SectionType.Chorus;
  }
}

export class SongVerseUI extends SongVerseDTO {
  selected: boolean;

  constructor(values: Object = {}) {
    super(values);
    Object.assign(this, values);
  }
}

export class Song extends BaseModel {

  static PUBLIC = "PUBLIC";
  static UPLOADED = "UPLOADED";
  static REVIEWER = "REVIEWER";
  private static currentDate = new Date().getTime();
  originalId: string;
  title = '';
  songVerseDTOS: SongVerseDTO[];
  private songVerses: SongVerseDTO[];
  modifiedDate: number;
  createdDate: number;
  deleted = false;
  uuid: '';
  languageDTO: Language;
  uploaded: Boolean;
  versionGroup: '';
  views = 0;
  favourites = 0;
  youtubeUrl;
  verseOrder: string;
  author: string;
  verseOrderList: number[];
  createdByEmail: string;
  backUpSongId: string;
  lastModifiedByUserEmail: string;
  commonWordsCount = 0;
  commonCharacterCount = 0;
  repeatChorus: boolean = true;

  constructor(values: Object = {}) {
    super(values);
    Object.assign(this, values);
  }

  static getScore(song) {
    let score = 0;
    if (song.views != null) {
      score += song.views;
    }
    if (song.favourites != null) {
      score += song.favourites * 3;
    }
    if (song.youtubeUrl != null) {
      score += 10;
    }
    let l = Song.getCurrentDate() - song.createdDate;
    if (l < 2592000000) {
      score += 14 * ((1 - l / 2592000000));
    }
    l = Song.getCurrentDate() - song.modifiedDate;
    if (l < 2592000000) {
      score += 4 * ((1 - l / 2592000000));
    }
    return score;
  }

  private static getCurrentDate() {
    return this.currentDate;
  }

  public static getNewSongForUI() {
    let song = new Song();
    song.title = 'Loading';
    song.songVerseDTOS = [];
    return song;
  }

  private getVersesByVerseOrder(repeatChorus: boolean): SongVerseDTO[] {
    let verseList: SongVerseDTO[] = [];
    let verses = this.songVerseDTOS;
    let size = verses.length;
    if (this.verseOrderList == undefined) {
      let chorus = new SongVerseDTO();
      for (let i = 0; i < size; ++i) {
        let songVerse = verses[i];
        verseList.push(songVerse);
        if (repeatChorus) {
          if (songVerse.chorus) {
            chorus = new SongVerseDTO();
            Object.assign(chorus, songVerse);
          } else if (chorus.text.length > 0 && chorus.chorus) {
            if (i + 1 < size) {
              if (!verses[i + 1].chorus) {
                let copyChorus = new SongVerseDTO();
                Object.assign(copyChorus, chorus);
                verseList.push(copyChorus);
              }
            } else {
              let copyChorus = new SongVerseDTO();
              Object.assign(copyChorus, chorus);
              verseList.push(copyChorus);
            }
          }
        }
      }
    } else {
      for (const verse of verses) {
        verse.was = false;
      }
      for (const i of this.verseOrderList) {
        if (i < size) {
          if (!verses[i].was) {
            verseList.push(verses[i]);
            verses[i].was = true;
          } else {
            let verse = new SongVerseDTO();
            Object.assign(verse, verses[i]);
            verseList.push(verse);
          }
        }
      }
    }
    return verseList;
  }

  public getVerses(): SongVerseDTO[] {
    if (this.songVerses == undefined) {
      this.songVerses = this.getVersesByVerseOrder(this.repeatChorus);
    }
    return this.songVerses;
  }
}

@Injectable()
export class SongService {

  constructor(private api: ApiService) {
  }

  // noinspection JSUnusedGlobalSymbols
  getAllSongs(): Observable<Song[]> {
    return this.api.getAll(Song, 'api/songs');
  }

  // noinspection JSUnusedGlobalSymbols
  getAllSongTitles() {
    return this.api.getAll(Song, 'api/songTitles');
  }

  // noinspection JSUnusedGlobalSymbols
  getSongByTitle(title: string) {
    return this.api.getAttribute(Song, 'api/song?title=' + title);
  }

  getSong(id) {
    return this.api.getById(Song, 'api/song/', id);
  }

  createSong(song: Song) {
    return this.api.create(Song, 'user/api/song', song);
  }

  getAllSongTitlesAfterModifiedDate(modifiedDate: number, selectedLanguage: any) {
    return this.api.getAll(Song, 'api/songTitlesAfterModifiedDate/' + modifiedDate + '/language/' + selectedLanguage);
  }

  getAllInReviewSongsByLanguage(selectedLanguage: Language) {
    return this.api.getAll(Song, 'api/songTitlesInReview/language/' + selectedLanguage.uuid);
  }

  deleteById(role: string, songId) {
    return this.api.deleteById(role + '/api/song/delete/', songId);
  }

  eraseById(role: string, songId) {
    return this.api.deleteById(role + '/api/song/erase/', songId);
  }

  updateSong(role: string, song: Song) {
    song.id = song.uuid;
    return this.api.update(Song, role + '/api/song/', song);
  }

  publishById(songId) {
    return this.api.deleteById('admin/api/song/publish/', songId);
  }

  getSimilar(song: Song) {
    return this.api.getAll(Song, 'api/songs/similar/song/' + song.uuid);
  }

  getSimilarByPost(song: Song) {
    return this.api.getAllByPost(Song, 'api/songs/similar/song', song);
  }

  getAllUploadedSongTitles() {
    return this.api.getAll(Song, 'api/songs/upload');
  }

  mergeVersionGroup(songId1, songId2, user) {
    return this.api.post(user + '/api/songVersionGroup/' + songId1 + '/' + songId2);
  }

  getSongsByVersionGroup(id) {
    return this.api.getAll(Song, '/api/songs/versionGroup/' + id);
  }

  changeLanguage(role: string, song: Song) {
    song.id = song.uuid;
    return this.api.update(Song, role + '/api/changeLanguageForSong/', song);
  }
}
