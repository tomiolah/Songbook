import {Injectable} from '@angular/core';
import {ApiService} from './api.service';
import {Observable} from 'rxjs/Observable';
import {BaseModel} from '../models/base-model';
import {Language} from "../models/language";

export class ColorText {
  text: string;
  color = false;
}

export class ColorLine {
  texts: ColorText[];
}

export class SongVerseDTO {
  lines: string[];
  colorLines: ColorLine[];
  text = '';
  chorus: boolean;

  constructor(values: Object = {}) {
    Object.assign(this, values);
  }
}

export class Song extends BaseModel {

  static PUBLIC = "PUBLIC";
  static UPLOADED = "UPLOADED";
  private static currentDate = new Date().getTime();
  originalId: string;
  title = '';
  songVerseDTOS: SongVerseDTO[];
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
    return this.api.create(Song, 'api/song', song);
  }

  getAllSongTitlesAfterModifiedDate(modifiedDate: number, selectedLanguage: any) {
    return this.api.getAll(Song, 'api/songTitlesAfterModifiedDate/' + modifiedDate + '/language/' + selectedLanguage);
  }

  deleteById(songId) {
    return this.api.deleteById('admin/api/song/delete/', songId);
  }

  eraseById(songId) {
    return this.api.deleteById('admin/api/song/erase/', songId);
  }

  updateSong(song: Song) {
    song.id = song.uuid;
    return this.api.update(Song, 'admin/api/song/', song);
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

  mergeVersionGroup(songId1, songId2) {
    return this.api.post('admin/api/songVersionGroup/' + songId1 + '/' + songId2);
  }

  getSongsByVersionGroup(id) {
    return this.api.getAll(Song, '/api/songs/versionGroup/' + id);
  }
}
