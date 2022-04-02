import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar, MatDialog } from '@angular/material';
import { Title } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { Language } from '../../models/language';
import { SongCollection } from '../../models/songCollection';
import { LanguageDataService } from '../../services/language-data.service';
import { SongCollectionDataService } from '../../services/song-collection-data.service';
import { AuthenticateComponent } from '../authenticate/authenticate.component';

@Component({
  selector: 'app-new-song-collection',
  templateUrl: './new-song-collection.component.html',
  styleUrls: ['./new-song-collection.component.css']
})
export class NewSongCollectionComponent implements OnInit {

  form: FormGroup;
  formErrors = {
    'title': '',
  };
  languages: Language[];
  selectedLanguage: Language = null;
  songCollection: SongCollection;

  constructor(
    private fb: FormBuilder,
    private songCollectionDataService: SongCollectionDataService,
    private languageDataService: LanguageDataService,
    private titleService: Title,
    private router: Router,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
  ) { }

  ngOnInit() {
    this.titleService.setTitle('New song collection');
    this.languages = [];
    this.createForm();
    this.loadLanguages();
  }

  private loadLanguages() {
    this.languageDataService.getAll().subscribe(
      (languages) => {
        this.languages = languages;
      }
    );
  }

  private createForm() {
    this.songCollection = new SongCollection();
    this.form = this.fb.group({
      'title': [this.songCollection.name, [
        Validators.required
      ]],
    });
  }

  onSubmit() {
    const formValue = this.form.value;
    this.songCollection.name = formValue.title;
    if (this.selectedLanguage == null) {
      return;
    }
    this.songCollection.languageUuid = this.selectedLanguage.uuid;
    this.songCollectionDataService.create(this.songCollection).subscribe(
      (_songCollection) => {
        // noinspection JSIgnoredPromiseFromCall
        this.router.navigate(['/songs']);
      },
      (err) => {
        if (err.message === 'Unexpected token < in JSON at position 0') {
          this.openAuthenticateDialog();
        } else {
          console.log(err);
          this.snackBar.open(err._body, 'Close', {
            duration: 5000
          })
        }
      }
    );
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
        this.onSubmit();
      }
    });
  }

}
