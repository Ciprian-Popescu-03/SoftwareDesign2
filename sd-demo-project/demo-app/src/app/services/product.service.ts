import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Product, CreateProductDto } from '../models/product.model';

const API_URL = 'http://localhost:8081/product'; // Make sure you have a ProductController in Spring Boot!

@Injectable({ providedIn: 'root' })
export class ProductService {
  private readonly http = inject(HttpClient);

  getAll(): Observable<Product[]> {
    return this.http.get<Product[]>(API_URL);
  }

  create(dto: CreateProductDto): Observable<Product> {
    return this.http.post<Product>(API_URL, dto);
  }

  update(id: string, dto: CreateProductDto): Observable<Product> {
    return this.http.put<Product>(`${API_URL}/${id}`, dto);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${API_URL}/${id}`);
  }
}
