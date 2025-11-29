import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
    selector: 'app-navbar',
    standalone: true,
    imports: [RouterLink, RouterLinkActive],
    template: `
    <nav class="navbar">
      <a routerLink="/" class="brand">🏠 Microservices App</a>
      <div class="nav-links">
        <a routerLink="/customers" routerLinkActive="active">Customers</a>
        <a routerLink="/products" routerLinkActive="active">Products</a>
        <a routerLink="/bills" routerLinkActive="active">Bills</a>
      </div>
    </nav>
  `,
    styles: [`
    .navbar {
      background: #343a40;
      padding: 0 20px;
      display: flex;
      align-items: center;
      height: 60px;
    }
    .brand {
      color: white;
      text-decoration: none;
      font-size: 18px;
      font-weight: bold;
      margin-right: 40px;
    }
    .nav-links a {
      color: #adb5bd;
      text-decoration: none;
      padding: 8px 16px;
      margin-right: 5px;
      border-radius: 4px;
      transition: background 0.2s, color 0.2s;
    }
    .nav-links a:hover {
      background: #495057;
      color: white;
    }
    .nav-links a.active {
      background: #007bff;
      color: white;
    }
  `]
})
export class NavbarComponent { }
