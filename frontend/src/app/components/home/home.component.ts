import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
    selector: 'app-home',
    standalone: true,
    imports: [RouterLink],
    template: `
    <div class="home">
      <h1>Microservices Dashboard</h1>
      <p>Welcome to the Microservices Management Application</p>
      
      <div class="cards">
        <a routerLink="/customers" class="card">
          <h3>👥 Customers</h3>
          <p>Manage customer records</p>
        </a>
        
        <a routerLink="/products" class="card">
          <h3>📦 Products</h3>
          <p>Manage product inventory</p>
        </a>
        
        <a routerLink="/bills" class="card">
          <h3>🧾 Bills</h3>
          <p>View and create bills</p>
        </a>
      </div>
    </div>
  `,
    styles: [`
    .home { padding: 40px; text-align: center; }
    .home h1 { color: #333; margin-bottom: 10px; }
    .home p { color: #666; margin-bottom: 40px; }
    .cards { display: flex; gap: 20px; justify-content: center; flex-wrap: wrap; }
    .card { 
      display: block;
      background: white; 
      border: 1px solid #ddd; 
      border-radius: 8px; 
      padding: 30px; 
      width: 200px;
      text-decoration: none;
      color: inherit;
      transition: box-shadow 0.2s, transform 0.2s;
    }
    .card:hover { 
      box-shadow: 0 4px 12px rgba(0,0,0,0.15); 
      transform: translateY(-2px);
    }
    .card h3 { margin: 0 0 10px 0; color: #007bff; }
    .card p { margin: 0; color: #666; font-size: 14px; }
  `]
})
export class HomeComponent { }
