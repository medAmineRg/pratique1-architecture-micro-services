import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { Product } from '../../models/product.model';
import { ProductService } from '../../services/product.service';

@Component({
    selector: 'app-product-form',
    standalone: true,
    imports: [CommonModule, FormsModule],
    template: `
    <div class="container">
      <h2>{{ isEdit ? 'Edit Product' : 'New Product' }}</h2>
      
      <form (ngSubmit)="onSubmit()" #productForm="ngForm">
        <div class="form-group">
          <label>Name *</label>
          <input type="text" [(ngModel)]="product.name" name="name" required class="form-control">
        </div>
        
        <div class="form-group">
          <label>Description</label>
          <input type="text" [(ngModel)]="product.description" name="description" class="form-control">
        </div>
        
        <div class="form-group">
          <label>Price *</label>
          <input type="number" [(ngModel)]="product.price" name="price" required min="0" step="0.01" class="form-control">
        </div>
        
        <div class="form-group">
          <label>Quantity *</label>
          <input type="number" [(ngModel)]="product.quantity" name="quantity" required min="0" class="form-control">
        </div>
        
        <div class="actions">
          <button type="submit" class="btn btn-primary" [disabled]="!productForm.valid">Save</button>
          <button type="button" class="btn" (click)="cancel()">Cancel</button>
        </div>
      </form>
    </div>
  `,
    styles: [`
    .container { padding: 20px; max-width: 500px; }
    .form-group { margin-bottom: 15px; }
    .form-group label { display: block; margin-bottom: 5px; font-weight: bold; }
    .form-control { width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box; }
    .actions { margin-top: 20px; }
    .btn { padding: 10px 20px; border-radius: 4px; cursor: pointer; border: none; margin-right: 10px; }
    .btn-primary { background: #007bff; color: white; }
    .btn-primary:disabled { background: #ccc; }
  `]
})
export class ProductFormComponent implements OnInit {
    product: Product = { name: '', price: 0, quantity: 0 };
    isEdit = false;

    constructor(
        private productService: ProductService,
        private router: Router,
        private route: ActivatedRoute
    ) { }

    ngOnInit() {
        const id = this.route.snapshot.params['id'];
        if (id) {
            this.isEdit = true;
            this.productService.getById(+id).subscribe({
                next: (data) => this.product = data,
                error: (err) => console.error('Error loading product', err)
            });
        }
    }

    onSubmit() {
        if (this.isEdit && this.product.id) {
            this.productService.update(this.product.id, this.product).subscribe({
                next: () => this.router.navigate(['/products']),
                error: (err) => console.error('Error updating product', err)
            });
        } else {
            this.productService.create(this.product).subscribe({
                next: () => this.router.navigate(['/products']),
                error: (err) => console.error('Error creating product', err)
            });
        }
    }

    cancel() {
        this.router.navigate(['/products']);
    }
}
