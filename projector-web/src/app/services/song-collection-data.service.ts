import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { ApiService } from './api.service';
import { SongCollection, SongCollectionElement } from "../models/songCollection";
import { Song } from './song-service.service';

@Injectable()
export class SongCollectionDataService {

  constructor(private api: ApiService) {
  }

  getAll(): Observable<SongCollection[]> {
    return this.api.getAll(SongCollection, 'api/songCollections');
  }

  getAllBySongId(songId: string): Observable<SongCollection[]> {
    return this.api.getAll(SongCollection, 'api/songCollections/song/' + songId);
  }

  putInCollection(songCollection: SongCollection, songCollectionElement: SongCollectionElement) {
    return this.api.put(SongCollectionElement, 'admin/api/songCollection/' + songCollection.uuid + '/songCollectionElement', songCollectionElement);
  }

  deleteSongCollectionElement(songCollectionElement: SongCollectionElement, songCollection: SongCollection, song: Song) {
    return this.api.deleteById('admin/api/songCollection/' + songCollection.uuid + '/song/' + song.uuid + '/ordinalNumber/', songCollectionElement.ordinalNumber);
  }
}
