import {SongVerseDTO} from '../services/song-service.service';
import {BaseModel} from './base-model';

export class Suggestion extends BaseModel {
  songId: string;
  title: string;
  verses: SongVerseDTO[];
  createdDate: Date;
  applied: boolean;
  createdByEmail: string;
  description: string;
  nr: number;
  youtubeUrl: string;

  constructor(values: Object = {}) {
    super(values);
    Object.assign(this, values);
  }
}
