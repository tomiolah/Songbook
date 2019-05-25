import { Component, OnInit } from '@angular/core';
import { AuthService } from "../../services/auth.service";

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
  ];
  userMenuTabs = [
    { link: '/songs', icon: 'menu', title: 'Songs' },
    { link: '/addNewSong', icon: 'add_box', title: 'Add song' },
  ];

  constructor(public auth: AuthService, ) {
  }

  ngOnInit() {
  }

}
