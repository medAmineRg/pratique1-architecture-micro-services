import { Routes } from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { CustomersComponent } from './components/customers/customers.component';
import { CustomerFormComponent } from './components/customers/customer-form.component';
import { ProductsComponent } from './components/products/products.component';
import { ProductFormComponent } from './components/products/product-form.component';
import { BillsComponent } from './components/bills/bills.component';
import { BillFormComponent } from './components/bills/bill-form.component';
import { BillDetailComponent } from './components/bills/bill-detail.component';

export const routes: Routes = [
    { path: '', component: HomeComponent },
    { path: 'customers', component: CustomersComponent },
    { path: 'customers/new', component: CustomerFormComponent },
    { path: 'customers/edit/:id', component: CustomerFormComponent },
    { path: 'products', component: ProductsComponent },
    { path: 'products/new', component: ProductFormComponent },
    { path: 'products/edit/:id', component: ProductFormComponent },
    { path: 'bills', component: BillsComponent },
    { path: 'bills/new', component: BillFormComponent },
    { path: 'bills/:id', component: BillDetailComponent },
    { path: '**', redirectTo: '' }
];
