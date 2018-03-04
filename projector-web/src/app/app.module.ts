import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {
  MatAutocompleteModule,
  MatButtonModule,
  MatButtonToggleModule,
  MatCardModule,
  MatCheckboxModule,
  MatChipsModule,
  MatDatepickerModule,
  MatDialogModule,
  MatExpansionModule,
  MatGridListModule,
  MatIconModule,
  MatInputModule,
  MatListModule,
  MatMenuModule,
  MatNativeDateModule,
  MatPaginatorModule,
  MatProgressBarModule,
  MatProgressSpinnerModule,
  MatRadioModule,
  MatRippleModule,
  MatSelectModule,
  MatSidenavModule,
  MatSliderModule,
  MatSlideToggleModule,
  MatSnackBarModule,
  MatSortModule,
  MatStepperModule,
  MatTableModule,
  MatTabsModule,
  MatToolbarModule,
  MatTooltipModule
} from '@angular/material';

import {AppComponent} from './app.component';
import {SongListComponent} from './ui/song-list/song-list.component';
import {SongService} from './services/song-service.service';
import {ApiService} from './services/api.service';
import {HttpModule} from '@angular/http';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {CdkTableModule} from '@angular/cdk/table';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {MenuTabsComponent} from './ui/menu-tabs/menu-tabs.component';
import {NotFoundComponent} from './ui/not-found/not-found.component';
import {AppRoutingModule} from './modules/app-routing.module';
import {NewSongComponent} from './ui/new-song/new-song.component';
import {UserDataService} from './services/user-data.service';
import {LoginComponent} from './ui/login/login.component';
import {AuthService} from './services/auth.service';
import {AuthGuard} from './services/auth-guard.service';
import {StatisticsListComponent} from './ui/statistics-list/statistics-list.component';
import {StatisticsDataService} from './services/statistics-data.service';
import {SongComponent} from './ui/song/song.component';
import {EditSongComponent} from './ui/edit-song/edit-song.component';
import {LanguageDataService} from "./services/language-data.service";
import {NewLanguageComponent} from "./ui/new-language/new-language.component";
import {CompareSongsComponent} from './ui/compare-songs/compare-songs.component';

@NgModule({
  exports: [
    CdkTableModule,
    MatAutocompleteModule,
    MatButtonModule,
    MatButtonToggleModule,
    MatCardModule,
    MatCheckboxModule,
    MatChipsModule,
    MatStepperModule,
    MatDatepickerModule,
    MatDialogModule,
    MatExpansionModule,
    MatGridListModule,
    MatIconModule,
    MatInputModule,
    MatListModule,
    MatMenuModule,
    MatNativeDateModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
    MatRadioModule,
    MatRippleModule,
    MatSelectModule,
    MatSidenavModule,
    MatSliderModule,
    MatSlideToggleModule,
    MatSnackBarModule,
    MatSortModule,
    MatTableModule,
    MatTabsModule,
    MatToolbarModule,
    MatTooltipModule,
  ]
})
export class PlunkerMaterialModule {
}

@NgModule({
  declarations: [
    AppComponent,
    SongListComponent,
    MenuTabsComponent,
    NotFoundComponent,
    NewSongComponent,
    LoginComponent,
    StatisticsListComponent,
    SongComponent,
    EditSongComponent,
    NewLanguageComponent,
    CompareSongsComponent,
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    MatCardModule,
    HttpModule,
    MatAutocompleteModule,
    PlunkerMaterialModule,
    FormsModule,
    ReactiveFormsModule,
    AppRoutingModule,
  ],
  entryComponents: [
    NewLanguageComponent,
  ],
  providers: [
    ApiService,
    AuthService,
    AuthGuard,
    SongService,
    UserDataService,
    StatisticsDataService,
    LanguageDataService,
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}