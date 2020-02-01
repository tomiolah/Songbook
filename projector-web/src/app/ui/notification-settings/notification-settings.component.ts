import { Component, OnInit } from '@angular/core';
import { Language } from '../../models/language';
import { LanguageDataService } from '../../services/language-data.service';
import { AuthService } from '../../services/auth.service';
import { LanguageNotification } from '../../models/languageNotification';
import { UserPropertiesDataService } from '../../services/user-properties-data.service';
import { UserProperties } from '../../models/userProperties';
import { Title } from '@angular/platform-browser';
import { MatDialog } from '@angular/material';
import { AuthenticateComponent } from '../authenticate/authenticate.component';

@Component({
  selector: 'app-notification-settings',
  templateUrl: './notification-settings.component.html',
  styleUrls: ['./notification-settings.component.css']
})
export class NotificationSettingsComponent implements OnInit {

  private languagesReview: LanguageNotification[] = [];
  notifications: LanguageNotification[] = [];
  userProperties: UserProperties;

  constructor(
    private languageDataService: LanguageDataService,
    private userPropertiesDataService: UserPropertiesDataService,
    private titleService: Title,
    private dialog: MatDialog,
    public auth: AuthService,
  ) { }

  ngOnInit() {
    this.titleService.setTitle('Notifications');
    this.loadUserProperties();
    this.languageDataService.getAll().subscribe(
      (languages) => {
        const user = this.auth.getUser();
        for (const language of languages) {
          if (user.hasReviewerRoleForLanguage(language)) {
            let languageNotification = new LanguageNotification();
            languageNotification.language = language;
            this.languagesReview.push(languageNotification);
          }
        }
        this.initializeNotifications();
      }
    );
  }

  private loadUserProperties() {
    this.userPropertiesDataService.get().subscribe(
      (userProperties) => {
        this.userProperties = userProperties;
        this.initializeNotifications();
      },
      (err) => {
        if (err.message === 'Unexpected token < in JSON at position 0') {
          this.openAuthenticateDialog();
        } else {
          console.log(err);
        }
      }
    )
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
        this.loadUserProperties();
      }
    });
  }

  initializeNotifications() {
    if (this.userProperties == undefined) {
      return;
    }
    if (this.languagesReview == undefined || this.languagesReview.length == 0) {
      return;
    }
    this.notifications = [];
    for (const notification of this.userProperties.notificationsByLanguage) {
      this.notifications.push(new LanguageNotification(notification));
    }
    for (const notification of this.languagesReview) {
      if (!this.containsInNotifications(notification, this.notifications)) {
        this.notifications.push(new LanguageNotification(notification));
      }
    }
  }

  containsInNotifications(notification: LanguageNotification, notifications: LanguageNotification[]) {
    for (const aNotification of notifications) {
      if (notification.language.uuid == aNotification.language.uuid) {
        return true;
      }
    }
    return false;
  }

  toggleSuggestionsNotification(languageNotification: LanguageNotification) {
    languageNotification.suggestions = !languageNotification.suggestions;
    this.update();
  }

  toggleNewSongsNotification(languageNotification: LanguageNotification) {
    languageNotification.newSongs = !languageNotification.newSongs;
    this.update();
  }

  getLanguageName(languageNotification: LanguageNotification) {
    const language = new Language(languageNotification.language);
    return language.printLanguage();
  }

  update() {
    this.userProperties.notificationsByLanguage = this.notifications;
    this.userPropertiesDataService.save(this.userProperties).subscribe((userProperties) => {

    },
    (err) => {
      if (err.status === 405) {
        this.openAuthenticateDialog();
      } else {
        console.log(err);
      }
    });
  }

}
