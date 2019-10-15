import {Component, OnInit} from '@angular/core';
import {BehaviorSubject} from 'rxjs/BehaviorSubject';
import {Observable} from 'rxjs/Observable';
import {DataSource} from '@angular/cdk/table';
import {Router} from '@angular/router';
import {SuggestionDataService} from '../../services/suggestion-data.service';
import {Suggestion} from '../../models/suggestion';
import {AuthenticateComponent} from "../authenticate/authenticate.component";
import {MatDialog} from "@angular/material";
import {AuthService} from "../../services/auth.service";
import {Title} from "@angular/platform-browser";

export class SuggestionDatabase {
  dataChange: BehaviorSubject<Suggestion[]> = new BehaviorSubject<Suggestion[]>([]);

  constructor(suggestionList: Suggestion[]) {
    if (suggestionList !== null) {
      const copiedData = this.data;
      let nr = 0;
      for (const suggestion of suggestionList) {
        suggestion.nr = ++nr;
        copiedData.push(suggestion);
        this.dataChange.next(copiedData);
      }

      this.dataChange.next(copiedData);
    }
  }

  get data(): Suggestion[] {
    return this.dataChange.value;
  }
}

export class SuggestionDataSource extends DataSource<any> {
  constructor(private _suggestionDatabase: SuggestionDatabase) {
    super();
  }

  connect(): Observable<Suggestion[]> {
    return this._suggestionDatabase.dataChange;
  }

  disconnect() {
  }
}

@Component({
  selector: 'app-suggestion-list',
  templateUrl: './suggestion-list.component.html',
  styleUrls: ['./suggestion-list.component.css']
})
export class SuggestionListComponent implements OnInit {

  suggestionList: Suggestion[] = [];
  displayedColumns = ['Nr', 'createdDate', 'title', 'description', 'email', 'applied'];
  suggestionDatabase: any;
  dataSource: SuggestionDataSource | null;

  constructor(public router: Router,
              private suggestionDataService: SuggestionDataService,
              private titleService: Title,
              private auth: AuthService,
              private dialog: MatDialog) {
  }

  ngOnInit() {
    this.titleService.setTitle('Suggestions');
    this.suggestionDataService.getAll().subscribe(
      (suggestionList) => {
        this.suggestionList = suggestionList.reverse();
        this.suggestionDatabase = new SuggestionDatabase(this.suggestionList);
        this.dataSource = new SuggestionDataSource(this.suggestionDatabase);
      },
      (err) => {
        if (err.message === 'Unexpected token < in JSON at position 0') {
          this.openAuthenticateDialog();
        }
      }
    );
  }

  onClick(row) {
    const suggestion = this.suggestionList[row.nr - 1];
    // noinspection JSIgnoredPromiseFromCall
    this.router.navigate(['/admin/suggestion/', suggestion.uuid]);
  }

  private openAuthenticateDialog() {
    let user = JSON.parse(localStorage.getItem('currentUser'));
    const dialogRef = this.dialog.open(AuthenticateComponent, {
      data: {
        email: user.email
      }
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result === 'ok') {
        this.ngOnInit();
      }
    });
  }
}
