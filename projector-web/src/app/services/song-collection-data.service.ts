import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {ApiService} from './api.service';
import {SongCollection, SongCollectionElement} from "../models/songCollection";

@Injectable()
export class SongCollectionDataService {

  constructor(private api: ApiService) {
  }

  getAll(): Observable<SongCollection[]> {
    return this.api.getAll(SongCollection, 'api/songCollections');
  }

  putInCollection(songCollection: SongCollection, songCollectionElement: SongCollectionElement) {
    return this.api.put(SongCollectionElement, 'admin/api/songCollection/' + songCollection.uuid + '/songCollectionElement', songCollectionElement);
  }
}
