import {Injectable} from '@angular/core';
import {ApiService} from './api.service';

@Injectable()
export class UserDataService {

  constructor(private api: ApiService) {
  }
}
