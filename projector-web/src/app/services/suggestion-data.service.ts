import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {ApiService} from './api.service';
import {Suggestion} from '../models/suggestion';

@Injectable()
export class SuggestionDataService {

  constructor(private api: ApiService) {
  }

  getAll(): Observable<Suggestion[]> {
    return this.api.getAll(Suggestion, 'admin/api/suggestions');
  }

  getSuggestion(suggestionId): Observable<Suggestion> {
    return this.api.getById(Suggestion, 'admin/api/suggestion/', suggestionId);
  }
}
