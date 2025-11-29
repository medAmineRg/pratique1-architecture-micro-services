import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Product } from '../../models/product.model';
import { ProductService } from '../../services/product.service';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule, RouterLink],
  host: { 'ngSkipHydration': 'true' },
  template: `
    <div class="container">
      <div class="header">
        <h2>Products</h2>
        <a routerLink="/products/new" class="btn btn-primary">+ Add Product</a>
      </div>

      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Description</th>
            <th>Price</th>
            <th>Quantity</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          @for (product of products(); track product.id) {
            <tr>
              <td>{{ product.id }}</td>
              <td>{{ product.name }}</td>
              <td>{{ product.description || '-' }}</td>
              <td>{{ product.price | currency }}</td>
              <td>{{ product.quantity }}</td>
              <td>
                <a [routerLink]="['/products/edit', product.id]" class="btn btn-sm">Edit</a>
                <button (click)="deleteProduct(product.id!)" class="btn btn-sm btn-danger">Delete</button>
              </td>
            </tr>
          } @empty {
            <tr>
              <td colspan="6" class="text-center">No products found</td>
            </tr>
          }
        </tbody>
      </table>
    </div>
  `,
  styles: [`
    .container { padding: 20px; }
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    .table { width: 100%; border-collapse: collapse; }
    .table th, .table td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
    .table th { background: #f5f5f5; }
    .btn { padding: 8px 16px; text-decoration: none; border-radius: 4px; cursor: pointer; border: none; }
    .btn-primary { background: #007bff; color: white; }
    .btn-sm { padding: 4px 8px; margin-right: 5px; background: #6c757d; color: white; }
    .btn-danger { background: #dc3545; }
    .text-center { text-align: center; }
  `]
})
export class ProductsComponent implements OnInit {
  products = signal<Product[]>([]);
  private readonly productService = inject(ProductService);

  ngOnInit() {
    this.loadProducts();
  }

  loadProducts() {
    this.productService.getAll().subscribe({
      next: (data) => this.products.set(data),
      error: (err) => console.error('Error loading products', err)
    });
  }

  deleteProduct(id: number) {
    if (confirm('Are you sure you want to delete this product?')) {
      this.productService.delete(id).subscribe({
        next: () => this.loadProducts(),
        error: (err) => console.error('Error deleting product', err)
      });
    }
  }
}
