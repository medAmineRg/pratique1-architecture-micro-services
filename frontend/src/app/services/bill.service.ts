import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Bill, BillDetail } from '../models/bill.model';

@Injectable({
    providedIn: 'root'
})
export class BillService {
    private apiUrl = 'http://localhost:8080/api/bills';

    constructor(private http: HttpClient) { }

    getAll(): Observable<Bill[]> {
        return this.http.get<Bill[]>(this.apiUrl);
    }

    getById(id: number): Observable<BillDetail> {
        return this.http.get<BillDetail>(`${this.apiUrl}/${id}`);
    }

    create(bill: { customerId: number; productId: number; quantity: number }): Observable<Bill> {
        return this.http.post<Bill>(this.apiUrl, bill);
    }

    delete(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }

    getByCustomer(customerId: number): Observable<Bill[]> {
        return this.http.get<Bill[]>(`${this.apiUrl}/customer/${customerId}`);
    }
}
