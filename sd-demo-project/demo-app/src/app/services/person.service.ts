import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreatePersonDto, Person, LoginDto } from '../models/person.model';

const API_URL = 'http://localhost:8081/person';
const AUTH_URL = 'http://localhost:8081/forgot-password';

// Interface to handle the new backend response
export interface AuthResponse {
  token: string;
  person: Person;
}

@Injectable({ providedIn: 'root' })
export class PersonService {
  private readonly http = inject(HttpClient);

  getAll(): Observable<Person[]> {
    return this.http.get<Person[]>(API_URL);
  }

  create(dto: CreatePersonDto): Observable<Person> {
    return this.http.post<Person>(API_URL, dto);
  }

  update(id: string, dto: CreatePersonDto): Observable<Person> {
    return this.http.put<Person>(`${API_URL}/${id}`, dto);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${API_URL}/${id}`);
  }

  // --- Assignment 3: Updated Login Method ---
  login(dto: LoginDto): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${API_URL}/login`, dto);
  }

  // --- Assignment 3: Forgot Password Methods ---
  requestPasswordReset(email: string): Observable<{ message: string }> {
    const params = new HttpParams().set('email', email);
    return this.http.post<{ message: string }>(`${AUTH_URL}/request`, null, { params });
  }

  resetPassword(email: string, code: string, newPassword: string): Observable<{ message: string }> {
    const params = new HttpParams()
      .set('email', email)
      .set('code', code)
      .set('newPassword', newPassword);
    return this.http.post<{ message: string }>(`${AUTH_URL}/reset`, null, { params });
  }
}
