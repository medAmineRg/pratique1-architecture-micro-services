import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { BillDetail } from '../../models/bill.model';
import { BillService } from '../../services/bill.service';

@Component({
    selector: 'app-bill-detail',
    standalone: true,
    imports: [CommonModule, RouterLink],
    template: `
    <div class="container">
      <a routerLink="/bills" class="back-link">← Back to Bills</a>
      
      @if (billDetail(); as detail) {
        <h2>Bill #{{ detail.bill.id }}</h2>
        
        <div class="card">
          <h3>Bill Information</h3>
          <p><strong>Quantity:</strong> {{ detail.bill.quantity }}</p>
          <p><strong>Total Amount:</strong> {{ detail.bill.totalAmount | currency }}</p>
          <p><strong>Created At:</strong> {{ detail.bill.createdAt | date:'medium' }}</p>
        </div>
        
        <div class="card">
          <h3>Customer</h3>
          @if (detail.customer) {
            <p><strong>Name:</strong> {{ detail.customer.name }}</p>
            <p><strong>Email:</strong> {{ detail.customer.email }}</p>
            <p><strong>Phone:</strong> {{ detail.customer.phone || '-' }}</p>
            <p><strong>Address:</strong> {{ detail.customer.address || '-' }}</p>
          } @else {
            <p class="error">{{ detail.customerError || 'Customer not found' }}</p>
          }
        </div>
        
        <div class="card">
          <h3>Product</h3>
          @if (detail.product) {
            <p><strong>Name:</strong> {{ detail.product.name }}</p>
            <p><strong>Description:</strong> {{ detail.product.description || '-' }}</p>
            <p><strong>Unit Price:</strong> {{ detail.product.price | currency }}</p>
          } @else {
            <p class="error">{{ detail.productError || 'Product not found' }}</p>
          }
        </div>
      } @else {
        <p>Loading...</p>
      }
    </div>
  `,
    styles: [`
    .container { padding: 20px; max-width: 600px; }
    .back-link { color: #007bff; text-decoration: none; display: inline-block; margin-bottom: 20px; }
    .card { background: #f9f9f9; border: 1px solid #ddd; border-radius: 8px; padding: 20px; margin-bottom: 20px; }
    .card h3 { margin-top: 0; border-bottom: 1px solid #ddd; padding-bottom: 10px; }
    .card p { margin: 8px 0; }
    .error { color: #dc3545; }
  `]
})
export class BillDetailComponent implements OnInit {
    billDetail = signal<BillDetail | null>(null);

    constructor(
        private billService: BillService,
        private route: ActivatedRoute
    ) { }

    ngOnInit() {
        this.route.params.subscribe(params => {
            const id = params['id'];
            if (id) {
                this.billService.getById(+id).subscribe({
                    next: (data) => {
                        this.billDetail.set(data);
                        console.log('Bill loaded:', data);
                    },
                    error: (err) => console.error('Error loading bill', err)
                });
            }
        });
    }
}
