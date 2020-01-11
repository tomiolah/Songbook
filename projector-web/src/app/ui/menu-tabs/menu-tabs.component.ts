import { Component, OnInit } from '@angular/core';
import { AuthService } from "../../services/auth.service";
import { User } from '../../models/user';

@Component({
  selector: 'app-menu-tabs',
  templateUrl: './menu-tabs.component.html',
  styleUrls: ['./menu-tabs.component.css']
})
export class MenuTabsComponent implements OnInit {

  menuTabs = [
    { link: '/songs', icon: 'menu', title: 'Songs' },
    { link: '/login', icon: 'person', title: 'Login' },
  ];
  adminMenuTabs = [
    { link: '/songs', icon: 'menu', title: 'Songs' },
    { link: '/addNewSong', icon: 'add_box', title: 'Add song' },
    { link: '/admin/suggestions', icon: 'announcement', title: 'Suggestions' },
    { link: '/admin/users', icon: 'supervised_user_circle', title: 'Users' },
  ];
  userMenuTabs = [
    { link: '/songs', icon: 'menu', title: 'Songs' },
    { link: '/addNewSong', icon: 'add_box', title: 'Add song' },
  ];
  reviewerMenuTabs = [
    { link: '/songs', icon: 'menu', title: 'Songs' },
    { link: '/addNewSong', icon: 'add_box', title: 'Add song' },
  ];

  constructor(public auth: AuthService, ) {
  }

  ngOnInit() {
  }

  isLoggedInUser() {
    const user: User = this.auth.getUser();
    return this.auth.isLoggedIn && user != undefined && user.role != undefined;
  }

  isAdmin(): Boolean {
    const user: User = this.auth.getUser();
    return this.isLoggedInUser() && user.isAdmin();
  }

  isUser(): Boolean {
    const user: User = this.auth.getUser();
    return this.isLoggedInUser() && user.isUser();
  }

  isReviewer(): Boolean {
    const user: User = this.auth.getUser();
    return this.isLoggedInUser() && user.isReviewer();
  }
}
