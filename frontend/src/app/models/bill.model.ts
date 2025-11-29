import { Customer } from './customer.model';
import { Product } from './product.model';

export interface Bill {
    id?: number;
    customerId: number;
    productId: number;
    quantity: number;
    totalAmount?: number;
    createdAt?: string;
}

export interface BillDetail {
    bill: Bill;
    customer?: Customer;
    customerError?: string;
    product?: Product;
    productError?: string;
}
