import { SelectivePreloadingStrategy } from './selective-preloading-strategy';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { NotFoundComponent } from '../ui/not-found/not-found.component';
import { NewSongComponent } from '../ui/new-song/new-song.component';
import { SongListComponent } from '../ui/song-list/song-list.component';
import { LoginComponent } from '../ui/login/login.component';
import { StatisticsListComponent } from '../ui/statistics-list/statistics-list.component';
import { SongComponent } from '../ui/song/song.component';
import { SuggestionListComponent } from "../ui/suggestion-list/suggestion-list.component";
import { SuggestionComponent } from "../ui/suggestion/suggestion.component";
import { UserRegisterComponent } from '../ui/user-register/user-register.component';
import { ActivationComponent } from '../ui/activation/activation.component';
import { UsersComponent } from '../ui/users/users.component';
import { UserComponent } from '../ui/user/user.component';
import { NotificationSettingsComponent } from '../ui/notification-settings/notification-settings.component';
import { ProjectorComponent } from '../ui/projector/projector.component';
import { ForgottenPasswordComponent } from '../ui/forgotten-password/forgotten-password.component';
import { ChangePasswordByTokenComponent } from '../ui/forgotten-password/change-password-by-token/change-password-by-token.component';
import { ReviewerStatisticsComponent } from '../ui/reviewer-statistics/reviewer-statistics.component';

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
    path: 'suggestion/:suggestionId',
    component: SuggestionComponent
  },
  {
    path: 'admin/users',
    component: UsersComponent
  },
  {
    path: 'admin/user/:userId',
    component: UserComponent
  },
  { path: 'addNewSong', component: NewSongComponent },
  { path: 'admin/statistics', component: StatisticsListComponent },
  { path: 'admin/reviewer-statistics/:userId', component: ReviewerStatisticsComponent },
  { path: 'suggestions', component: SuggestionListComponent },
  { path: 'songs', component: SongListComponent },
  { path: '', redirectTo: 'songs', pathMatch: 'full' },
  { path: 'registration', component: UserRegisterComponent },
  { path: 'activation/:code', component: ActivationComponent },
  { path: 'user/notifications', component: NotificationSettingsComponent },
  { path: 'desktop-app', component: ProjectorComponent },
  { path: 'forgottenPassword', component: ForgottenPasswordComponent },
  { path: 'changePasswordByToken', component: ChangePasswordByTokenComponent },
  { path: '**', component: NotFoundComponent },
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
