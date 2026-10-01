import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';

interface SearchCriteria {
  id: string;
  name: string;
  city: string;
  postalCode: string | null;
  radiusKm: number;
  maxPrice: number | null;
  minSurface: number | null;
  propertyType: string | null;
  active: boolean;
}

@Component({
  selector: 'app-root',
  standalone: true,
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {

  private readonly http = inject(HttpClient);

  protected readonly apiStatus = signal('Connexion...');
  protected readonly criteria = signal<SearchCriteria[]>([]);

  ngOnInit(): void {
    this.refresh();
  }

  protected refresh(): void {
    this.http.get<{ status: string }>('/api/health').subscribe({
      next: (response) => this.apiStatus.set(response.status),
      error: () => this.apiStatus.set('INDISPONIBLE')
    });

    this.http.get<SearchCriteria[]>('/api/search-criteria').subscribe({
      next: (response) => this.criteria.set(response),
      error: () => this.criteria.set([])
    });
  }
}
