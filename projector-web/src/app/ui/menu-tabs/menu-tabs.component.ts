import {Component, OnInit} from '@angular/core';
import {AuthService} from "../../services/auth.service";

@Component({
  selector: 'app-menu-tabs',
  templateUrl: './menu-tabs.component.html',
  styleUrls: ['./menu-tabs.component.css']
})
export class MenuTabsComponent implements OnInit {

  menuTabs = [
    {link: '/songs', icon: 'menu', title: 'Songs'},
    {link: '/addNewSong', icon: 'add_box', title: 'Add song'},
  ];
  adminMenuTabs = [
    {link: '/songs', icon: 'menu', title: 'Songs'},
    {link: '/addNewSong', icon: 'add_box', title: 'Add song'},
    {link: '/admin/suggestions', icon: 'add_box', title: 'Suggestions'},
  ];

  constructor(public auth: AuthService,) {
  }

  ngOnInit() {
  }

}
