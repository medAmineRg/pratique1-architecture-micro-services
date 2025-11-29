import { Component, afterNextRender } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Customer } from '../../models/customer.model';
import { CustomerService } from '../../services/customer.service';

@Component({
    selector: 'app-customers',
    standalone: true,
    imports: [CommonModule, RouterLink],
    template: `
    <div class="container">
      <div class="header">
        <h2>Customers</h2>
        <a routerLink="/customers/new" class="btn btn-primary">+ Add Customer</a>
      </div>

      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Email</th>
            <th>Phone</th>
            <th>Address</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          @for (customer of customers; track customer.id) {
            <tr>
              <td>{{ customer.id }}</td>
              <td>{{ customer.name }}</td>
              <td>{{ customer.email }}</td>
              <td>{{ customer.phone || '-' }}</td>
              <td>{{ customer.address || '-' }}</td>
              <td>
                <a [routerLink]="['/customers/edit', customer.id]" class="btn btn-sm">Edit</a>
                <button (click)="deleteCustomer(customer.id!)" class="btn btn-sm btn-danger">Delete</button>
              </td>
            </tr>
          } @empty {
            <tr>
              <td colspan="6" class="text-center">No customers found</td>
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
export class CustomersComponent {
    customers: Customer[] = [];

    constructor(private customerService: CustomerService) {
        afterNextRender(() => {
            this.loadCustomers();
        });
    }

    loadCustomers() {
        this.customerService.getAll().subscribe({
            next: (data) => this.customers = data,
            error: (err) => console.error('Error loading customers', err)
        });
    }

    deleteCustomer(id: number) {
        if (confirm('Are you sure you want to delete this customer?')) {
            this.customerService.delete(id).subscribe({
                next: () => this.loadCustomers(),
                error: (err) => console.error('Error deleting customer', err)
            });
        }
    }
}
