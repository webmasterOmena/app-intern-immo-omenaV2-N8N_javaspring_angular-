import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
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

interface ServiceHealth {
  id: string;
  name: string;
  type: string;
  status: 'UP' | 'DOWN' | 'NOT_CONFIGURED' | 'UNKNOWN';
  optional: boolean;
  latencyMs: number;
  detail: string;
}

interface SystemHealthResponse {
  status: 'UP' | 'DEGRADED' | 'DOWN';
  checkedAt: string;
  services: ServiceHealth[];
}

@Component({
  selector: 'app-root',
  standalone: true,
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit, OnDestroy {

  private readonly http = inject(HttpClient);
  private refreshTimer?: ReturnType<typeof setInterval>;

  protected readonly criteria = signal<SearchCriteria[]>([]);
  protected readonly systemStatus = signal<'LOADING' | 'UP' | 'DEGRADED' | 'DOWN'>('LOADING');
  protected readonly services = signal<ServiceHealth[]>([]);
  protected readonly checkedAt = signal<string | null>(null);
  protected readonly healthLoading = signal(false);

  protected readonly upCount = computed(
    () => 1 + this.services().filter((service) => service.status === 'UP').length
  );

  protected readonly totalCount = computed(
    () => 1 + this.services().length
  );

  ngOnInit(): void {
    this.refresh();

    this.refreshTimer = setInterval(
      () => this.refreshSystemHealth(),
      10_000
    );
  }

  ngOnDestroy(): void {
    if (this.refreshTimer) {
      clearInterval(this.refreshTimer);
    }
  }

  protected refresh(): void {
    this.refreshSystemHealth();
    this.refreshCriteria();
  }

  protected refreshSystemHealth(): void {
    this.healthLoading.set(true);

    this.http.get<SystemHealthResponse>('/api/system/health').subscribe({
      next: (response) => {
        this.systemStatus.set(response.status);
        this.services.set(response.services);
        this.checkedAt.set(response.checkedAt);
        this.healthLoading.set(false);
      },
      error: () => {
        this.systemStatus.set('DOWN');
        this.checkedAt.set(new Date().toISOString());
        this.services.set([
          {
            id: 'immo-api',
            name: 'API Spring Boot',
            type: 'Application',
            status: 'DOWN',
            optional: false,
            latencyMs: -1,
            detail: 'Impossible de joindre /api/system/health'
          },
          {
            id: 'immo-db',
            name: 'PostgreSQL métier',
            type: 'Base de données',
            status: 'UNKNOWN',
            optional: false,
            latencyMs: -1,
            detail: 'État inconnu tant que l’API est indisponible'
          },
          {
            id: 'n8n',
            name: 'n8n',
            type: 'Automatisation',
            status: 'UNKNOWN',
            optional: false,
            latencyMs: -1,
            detail: 'État inconnu tant que l’API est indisponible'
          },
          {
            id: 'n8n-db',
            name: 'PostgreSQL n8n',
            type: 'Base de données',
            status: 'UNKNOWN',
            optional: false,
            latencyMs: -1,
            detail: 'État inconnu tant que l’API est indisponible'
          }
        ]);
        this.healthLoading.set(false);
      }
    });
  }

  protected refreshCriteria(): void {
    this.http.get<SearchCriteria[]>('/api/search-criteria').subscribe({
      next: (response) => this.criteria.set(response),
      error: () => this.criteria.set([])
    });
  }

  protected statusLabel(status: ServiceHealth['status'] | 'LOADING'): string {
    switch (status) {
      case 'UP':
        return 'UP';
      case 'DOWN':
        return 'DOWN';
      case 'NOT_CONFIGURED':
        return 'NON CONFIGURÉ';
      case 'UNKNOWN':
        return 'INCONNU';
      default:
        return 'VÉRIFICATION';
    }
  }

  protected checkedAtLabel(): string {
    const value = this.checkedAt();

    if (!value) {
      return 'jamais';
    }

    return new Date(value).toLocaleTimeString('fr-FR');
  }
}
