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
  chorus: false;

  constructor(values: Object = {}) {
    Object.assign(this, values);
  }
}

export class Song extends BaseModel {
  title = '';
  songVerseDTOS: SongVerseDTO[];
  modifiedDate: number;
  deleted = false;
  uuid: '';
  languageDTO: Language;
  uploaded: Boolean;

  constructor(values: Object = {}) {
    super(values);
    Object.assign(this, values);
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

  getSong(id: number) {
    return this.api.getById(Song, 'api/song/', id);
  }

  createSong(song: Song) {
    return this.api.create(Song, 'api/song', song);
  }

  getAllSongTitlesAfterModifiedDate(modifiedDate: number) {
    return this.api.getAll(Song, 'api/songTitlesAfterModifiedDate/' + modifiedDate);
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

  getAllUploadedSongTitles() {
    return this.api.getAll(Song, 'api/songs/upload');
  }
}
