import { Component, afterNextRender } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { Customer } from '../../models/customer.model';
import { CustomerService } from '../../services/customer.service';

@Component({
    selector: 'app-customer-form',
    standalone: true,
    imports: [CommonModule, FormsModule],
    template: `
    <div class="container">
      <h2>{{ isEdit ? 'Edit Customer' : 'New Customer' }}</h2>
      
      <form (ngSubmit)="onSubmit()" #customerForm="ngForm">
        <div class="form-group">
          <label>Name *</label>
          <input type="text" [(ngModel)]="customer.name" name="name" required class="form-control">
        </div>
        
        <div class="form-group">
          <label>Email *</label>
          <input type="email" [(ngModel)]="customer.email" name="email" required class="form-control">
        </div>
        
        <div class="form-group">
          <label>Phone</label>
          <input type="text" [(ngModel)]="customer.phone" name="phone" class="form-control">
        </div>
        
        <div class="form-group">
          <label>Address</label>
          <input type="text" [(ngModel)]="customer.address" name="address" class="form-control">
        </div>
        
        <div class="actions">
          <button type="submit" class="btn btn-primary" [disabled]="!customerForm.valid">Save</button>
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
export class CustomerFormComponent {
    customer: Customer = { name: '', email: '' };
    isEdit = false;

    constructor(
        private customerService: CustomerService,
        private router: Router,
        private route: ActivatedRoute
    ) {
        afterNextRender(() => {
            const id = this.route.snapshot.params['id'];
            if (id) {
                this.isEdit = true;
                this.customerService.getById(+id).subscribe({
                    next: (data) => this.customer = data,
                    error: (err) => console.error('Error loading customer', err)
                });
            }
        });
    }

    onSubmit() {
        if (this.isEdit && this.customer.id) {
            this.customerService.update(this.customer.id, this.customer).subscribe({
                next: () => this.router.navigate(['/customers']),
                error: (err) => console.error('Error updating customer', err)
            });
        } else {
            this.customerService.create(this.customer).subscribe({
                next: () => this.router.navigate(['/customers']),
                error: (err) => console.error('Error creating customer', err)
            });
        }
    }

    cancel() {
        this.router.navigate(['/customers']);
    }
}
