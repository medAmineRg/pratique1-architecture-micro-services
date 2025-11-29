import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Customer } from '../../models/customer.model';
import { Product } from '../../models/product.model';
import { CustomerService } from '../../services/customer.service';
import { ProductService } from '../../services/product.service';
import { BillService } from '../../services/bill.service';

@Component({
    selector: 'app-bill-form',
    standalone: true,
    imports: [CommonModule, FormsModule],
    template: `
    <div class="container">
      <h2>Create New Bill</h2>
      
      <form (ngSubmit)="onSubmit()" #billForm="ngForm">
        <div class="form-group">
          <label>Customer *</label>
          <select [(ngModel)]="bill.customerId" name="customerId" required class="form-control">
            <option [ngValue]="null">-- Select Customer --</option>
            @for (customer of customers; track customer.id) {
              <option [ngValue]="customer.id">{{ customer.name }} ({{ customer.email }})</option>
            }
          </select>
        </div>
        
        <div class="form-group">
          <label>Product *</label>
          <select [(ngModel)]="bill.productId" name="productId" required class="form-control">
            <option [ngValue]="null">-- Select Product --</option>
            @for (product of products; track product.id) {
              <option [ngValue]="product.id">{{ product.name }} - {{ product.price | currency }} (Stock: {{ product.quantity }})</option>
            }
          </select>
        </div>
        
        <div class="form-group">
          <label>Quantity *</label>
          <input type="number" [(ngModel)]="bill.quantity" name="quantity" required min="1" class="form-control">
        </div>
        
        <div class="actions">
          <button type="submit" class="btn btn-primary" [disabled]="!billForm.valid">Create Bill</button>
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
export class BillFormComponent implements OnInit {
    bill = { customerId: null as number | null, productId: null as number | null, quantity: 1 };
    customers: Customer[] = [];
    products: Product[] = [];

    constructor(
        private customerService: CustomerService,
        private productService: ProductService,
        private billService: BillService,
        private router: Router
    ) { }

    ngOnInit() {
        this.customerService.getAll().subscribe({
            next: (data) => this.customers = data,
            error: (err) => console.error('Error loading customers', err)
        });

        this.productService.getAvailable().subscribe({
            next: (data) => this.products = data,
            error: (err) => console.error('Error loading products', err)
        });
    }

    onSubmit() {
        if (this.bill.customerId && this.bill.productId) {
            this.billService.create({
                customerId: this.bill.customerId,
                productId: this.bill.productId,
                quantity: this.bill.quantity
            }).subscribe({
                next: () => this.router.navigate(['/bills']),
                error: (err) => console.error('Error creating bill', err)
            });
        }
    }

    cancel() {
        this.router.navigate(['/bills']);
    }
}
