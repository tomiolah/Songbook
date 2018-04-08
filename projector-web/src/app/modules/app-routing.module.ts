import {SelectivePreloadingStrategy} from './selective-preloading-strategy';
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {NotFoundComponent} from '../ui/not-found/not-found.component';
import {NewSongComponent} from '../ui/new-song/new-song.component';
import {SongListComponent} from '../ui/song-list/song-list.component';
import {LoginComponent} from '../ui/login/login.component';
import {StatisticsListComponent} from '../ui/statistics-list/statistics-list.component';
import {SongComponent} from '../ui/song/song.component';
import {SuggestionListComponent} from "../ui/suggestion-list/suggestion-list.component";
import {SuggestionComponent} from "../ui/suggestion/suggestion.component";

export const appRoutes: Routes = [
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: 'login/:email',
    component: LoginComponent
  },
  {
    path: 'song/:id',
    component: SongComponent
  },
  {
    path: 'admin/suggestion/:suggestionId',
    component: SuggestionComponent
  },
  {path: 'addNewSong', component: NewSongComponent},
  {path: 'admin/statistics', component: StatisticsListComponent},
  {path: 'admin/suggestions', component: SuggestionListComponent},
  {path: 'songs', component: SongListComponent},
  {path: '', redirectTo: 'songs', pathMatch: 'full'},
  {path: '**', component: NotFoundComponent}
];

@NgModule({
  imports: [
    RouterModule.forRoot(
      appRoutes,
      {
        enableTracing: false, // <-- debugging purposes only
        preloadingStrategy: SelectivePreloadingStrategy,
        useHash: true
      }
    )
  ],
  exports: [
    RouterModule
  ],
  providers: [
    SelectivePreloadingStrategy
  ]
})
export class AppRoutingModule {
}
