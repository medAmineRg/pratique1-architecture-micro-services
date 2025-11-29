import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Bill } from '../../models/bill.model';
import { BillService } from '../../services/bill.service';

@Component({
  selector: 'app-bills',
  standalone: true,
  imports: [CommonModule, RouterLink],
  host: { 'ngSkipHydration': 'true' },
  template: `
    <div class="container">
      <div class="header">
        <h2>Bills</h2>
        <a routerLink="/bills/new" class="btn btn-primary">+ Create Bill</a>
      </div>

      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Customer ID</th>
            <th>Product ID</th>
            <th>Quantity</th>
            <th>Total Amount</th>
            <th>Created At</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          @for (bill of bills; track bill.id) {
            <tr>
              <td>{{ bill.id }}</td>
              <td>{{ bill.customerId }}</td>
              <td>{{ bill.productId }}</td>
              <td>{{ bill.quantity }}</td>
              <td>{{ bill.totalAmount | currency }}</td>
              <td>{{ bill.createdAt | date:'short' }}</td>
              <td>
                <a [routerLink]="['/bills', bill.id]" class="btn btn-sm">View</a>
                <button (click)="deleteBill(bill.id!)" class="btn btn-sm btn-danger">Delete</button>
              </td>
            </tr>
          } @empty {
            <tr>
              <td colspan="7" class="text-center">No bills found</td>
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
export class BillsComponent implements OnInit {
  bills: Bill[] = [];
  private readonly billService = inject(BillService);

  ngOnInit() {
    this.loadBills();
  }

  loadBills() {
    this.billService.getAll().subscribe({
      next: (data) => {
        console.log('Raw bills data:', data);
        // Map snake_case to camelCase if needed
        this.bills = data.map((bill: any) => ({
          id: bill.id,
          customerId: bill.customerId ?? bill.customer_id,
          productId: bill.productId ?? bill.product_id,
          quantity: bill.quantity,
          totalAmount: bill.totalAmount ?? bill.total_amount,
          createdAt: bill.createdAt ?? bill.created_at
        }));
        console.log('Mapped bills:', this.bills);
      },
      error: (err) => console.error('Error loading bills', err)
    });
  }

  deleteBill(id: number) {
    if (confirm('Are you sure you want to delete this bill?')) {
      this.billService.delete(id).subscribe({
        next: () => this.loadBills(),
        error: (err) => console.error('Error deleting bill', err)
      });
    }
  }
}
