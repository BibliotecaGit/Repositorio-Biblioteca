/**
 * Aurora - Cliente de Integração com API REST
 * Gerencia autenticação (JWT), requisições HTTP e sincronização de dados.
 */

const API_BASE = window.AURORA_API_BASE || 'http://localhost:8080/api';

const AuroraAPI = {
  // ---------------------------------------------------------------------------
  // 1. Gerenciamento de Token e Sessão
  // ---------------------------------------------------------------------------
  getToken() {
    return localStorage.getItem('aurora_token') || sessionStorage.getItem('aurora_token');
  },

  setToken(token, remember = true) {
    if (remember) {
      localStorage.setItem('aurora_token', token);
      sessionStorage.removeItem('aurora_token');
    } else {
      sessionStorage.setItem('aurora_token', token);
      localStorage.removeItem('aurora_token');
    }
  },

  removeToken() {
    localStorage.removeItem('aurora_token');
    sessionStorage.removeItem('aurora_token');
    localStorage.removeItem('aurora_user');
    sessionStorage.removeItem('aurora_user');
  },

  getUser() {
    const raw = localStorage.getItem('aurora_user') || sessionStorage.getItem('aurora_user');
    try {
      return raw ? JSON.parse(raw) : null;
    } catch (e) {
      return null;
    }
  },

  setUser(user, remember = true) {
    const data = JSON.stringify(user);
    if (remember || localStorage.getItem('aurora_token')) {
      localStorage.setItem('aurora_user', data);
    } else {
      sessionStorage.setItem('aurora_user', data);
    }
  },

  isAuthenticated() {
    return !!this.getToken();
  },

  // ---------------------------------------------------------------------------
  // 2. Fetch Centralizado com Injeção de Bearer Token e Tratamento de 401
  // ---------------------------------------------------------------------------
  async apiFetch(endpoint, options = {}) {
    const url = endpoint.startsWith('http') ? endpoint : `${API_BASE}${endpoint.startsWith('/') ? '' : '/'}${endpoint}`;
    
    const headers = {
      'Content-Type': 'application/json',
      ...(options.headers || {})
    };

    const token = this.getToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    try {
      const response = await fetch(url, {
        ...options,
        headers
      });

      if (response.status === 401) {
        // Se a sessão expirou e não estamos na tela de login, redireciona
        if (!window.location.pathname.endsWith('login.html') && !window.location.pathname.endsWith('1 - Cadastro.html')) {
          this.removeToken();
          window.location.href = 'login.html?expired=true';
          return null;
        }
      }

      // Trata respostas vazias (204 No Content ou delete)
      const contentType = response.headers.get('content-type');
      let data = null;
      if (contentType && contentType.includes('application/json')) {
        data = await response.json();
      } else {
        const text = await response.text();
        data = text ? { message: text } : {};
      }

      if (!response.ok) {
        const errorMessage = (data && (data.message || data.error)) || `Erro na requisição (${response.status})`;
        const error = new Error(errorMessage);
        error.status = response.status;
        error.data = data;
        throw error;
      }

      return data;
    } catch (err) {
      console.error(`[AuroraAPI Error] ${options.method || 'GET'} ${url}:`, err);
      throw err;
    }
  },

  // ---------------------------------------------------------------------------
  // 3. Módulo de Autenticação
  // ---------------------------------------------------------------------------
  async register(nome, email, senha, remember = true) {
    const data = await this.apiFetch('/auth/register', {
      method: 'POST',
      body: JSON.stringify({ nome, email, senha })
    });
    if (data.token) {
      this.setToken(data.token, remember);
      if (data.user) this.setUser(data.user, remember);
    }
    return data;
  },

  async login(email, senha, remember = true) {
    const data = await this.apiFetch('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, senha })
    });
    if (data.token) {
      this.setToken(data.token, remember);
      if (data.user) this.setUser(data.user, remember);
    }
    return data;
  },

  async getMe() {
    try {
      const user = await this.apiFetch('/auth/me');
      if (user) {
        this.setUser(user);
      }
      return user;
    } catch (e) {
      return null;
    }
  },

  logout() {
    this.removeToken();
    window.location.href = 'login.html';
  },

  // ---------------------------------------------------------------------------
  // 4. Módulo de Livros (Catálogo, Busca e Filtros)
  // ---------------------------------------------------------------------------
  async getBooks(params = {}) {
    const query = new URLSearchParams();
    if (params.search) query.append('search', params.search);
    if (params.tamanho && params.tamanho !== 'all') query.append('tamanho', params.tamanho);
    if (params.genero) query.append('genero', params.genero);
    if (params.autor) query.append('autor', params.autor);
    if (params.novidades) query.append('novidades', 'true');
    if (params.destaques) query.append('destaques', 'true');
    if (params.infantis) query.append('infantis', 'true');

    const qs = query.toString();
    return await this.apiFetch(`/books${qs ? '?' + qs : ''}`);
  },

  async getBookById(id) {
    return await this.apiFetch(`/books/${id}`);
  },

  async getNovidades() {
    return await this.apiFetch('/books/novidades');
  },

  async getDestaques() {
    return await this.apiFetch('/books/destaques');
  },

  async getInfantis() {
    return await this.apiFetch('/books/infantis');
  },

  // ---------------------------------------------------------------------------
  // 5. Módulo de Aluguéis e Extensões
  // ---------------------------------------------------------------------------
  async rentBook(bookId) {
    return await this.apiFetch(`/rentals/book/${bookId}`, {
      method: 'POST'
    });
  },

  async getMyRentals() {
    return await this.apiFetch('/rentals/my-rentals');
  },

  async extendRental(rentalId) {
    return await this.apiFetch(`/rentals/${rentalId}/extend`, {
      method: 'POST'
    });
  },

  async returnRental(rentalId) {
    return await this.apiFetch(`/rentals/${rentalId}/return`, {
      method: 'POST'
    });
  },

  // ---------------------------------------------------------------------------
  // 6. Módulo de Coleções (Já Lidos, Lista de Desejos, Avaliações)
  // ---------------------------------------------------------------------------
  async getReadBooks() {
    return await this.apiFetch('/collections/read');
  },

  async markAsRead(bookId) {
    return await this.apiFetch(`/collections/read/${bookId}`, {
      method: 'POST'
    });
  },

  async unmarkAsRead(bookId) {
    return await this.apiFetch(`/collections/read/${bookId}`, {
      method: 'DELETE'
    });
  },

  async getWishlist() {
    return await this.apiFetch('/collections/wishlist');
  },

  async addToWishlist(bookId) {
    return await this.apiFetch(`/collections/wishlist/${bookId}`, {
      method: 'POST'
    });
  },

  async removeFromWishlist(bookId) {
    return await this.apiFetch(`/collections/wishlist/${bookId}`, {
      method: 'DELETE'
    });
  },

  async rateBook(bookId, nota, comentario = '') {
    return await this.apiFetch(`/collections/ratings/book/${bookId}`, {
      method: 'POST',
      body: JSON.stringify({ nota, comentario })
    });
  },

  async getMyRatings() {
    return await this.apiFetch('/collections/ratings/my-ratings');
  },

  // ---------------------------------------------------------------------------
  // 7. Módulo de Sugestões e Multas
  // ---------------------------------------------------------------------------
  async getSuggestions() {
    return await this.apiFetch('/suggestions');
  },

  async recordFilterUsage(filterType, valor) {
    return await this.apiFetch(`/suggestions/record-filter?filterType=${filterType}&valor=${encodeURIComponent(valor)}`, {
      method: 'POST'
    });
  },

  async getMyFines() {
    return await this.apiFetch('/fines/my-fines');
  },

  async payFine(fineId) {
    return await this.apiFetch(`/fines/${fineId}/pay`, {
      method: 'POST'
    });
  },

  // ---------------------------------------------------------------------------
  // 8. Módulo de Usuário e Perfil
  // ---------------------------------------------------------------------------
  async getProfile() {
    return await this.apiFetch('/user/profile');
  },

  async updateProfile(profileData) {
    return await this.apiFetch('/user/profile', {
      method: 'PUT',
      body: JSON.stringify(profileData)
    });
  },

  // ---------------------------------------------------------------------------
  // 9. Toast de Notificações Global
  // ---------------------------------------------------------------------------
  showToast(message, icon = 'info', isError = false) {
    let toast = document.getElementById('toastNotification');
    if (!toast) {
      toast = document.createElement('div');
      toast.id = 'toastNotification';
      toast.className = 'fixed bottom-6 right-6 z-50 transform translate-y-24 opacity-0 transition-all duration-300 flex items-center gap-3 px-4 py-3 rounded-2xl shadow-xl backdrop-blur-md';
      toast.innerHTML = `
        <span id="toastIcon" class="material-symbols-outlined text-2xl"></span>
        <span id="toastMsg" class="font-body-md text-sm font-semibold"></span>
      `;
      document.body.appendChild(toast);
    }

    const toastIcon = toast.querySelector('#toastIcon');
    const toastMsg = toast.querySelector('#toastMsg');

    if (isError) {
      toast.className = 'fixed bottom-6 right-6 z-50 transform translate-y-0 opacity-100 transition-all duration-300 flex items-center gap-3 px-4 py-3 rounded-2xl shadow-xl bg-error text-on-error';
      toastIcon.textContent = icon || 'error';
    } else {
      toast.className = 'fixed bottom-6 right-6 z-50 transform translate-y-0 opacity-100 transition-all duration-300 flex items-center gap-3 px-4 py-3 rounded-2xl shadow-xl bg-primary-container text-on-primary';
      toastIcon.textContent = icon || 'check_circle';
      toastIcon.className = 'material-symbols-outlined text-2xl text-secondary-fixed';
    }

    toastMsg.textContent = message;

    clearTimeout(this._toastTimeout);
    this._toastTimeout = setTimeout(() => {
      toast.classList.remove('translate-y-0', 'opacity-100');
      toast.classList.add('translate-y-24', 'opacity-0');
    }, 3200);
  },

  // ---------------------------------------------------------------------------
  // 10. Inicialização da Sidebar e Proteção de Rota
  // ---------------------------------------------------------------------------
  async initSidebar(activePath = '') {
    // Se não estiver autenticado e não for a tela de login, redireciona
    const isLoginPage = window.location.pathname.endsWith('login.html') || window.location.pathname.endsWith('1 - Cadastro.html');
    if (!this.isAuthenticated()) {
      if (!isLoginPage) {
        window.location.href = 'login.html';
        return;
      }
      return;
    } else if (isLoginPage) {
      // Se já estiver logado na tela de login, redireciona para a Início
      window.location.href = 'index.html';
      return;
    }

    // Configura botões de logout
    document.querySelectorAll('[data-path="login"], .logout-btn').forEach(btn => {
      btn.href = '#';
      btn.addEventListener('click', (e) => {
        e.preventDefault();
        this.logout();
      });
    });

    // Mapeamento de links corretos da navegação
    const routes = {
      'inicio': 'index.html',
      'prateleira': 'prateleira.html',
      'sugestoes': 'sugestoes.html',
      'ja-lidos': 'ja-lidos.html',
      'lista-de-desejos': 'desejos.html',
      'avaliar-livros': 'avaliar.html',
      'configuracoes': 'configuracoes.html',
      'sacola-de-alugueis': 'index.html#alugueis-ativos'
    };

    document.querySelectorAll('nav a[data-path]').forEach(link => {
      const path = link.getAttribute('data-path');
      if (routes[path]) {
        link.href = routes[path];
      }

      // Marcação do link ativo
      if (path === activePath) {
        link.className = 'flex items-center justify-between px-space-md py-space-sm rounded-xl bg-primary-container text-on-primary font-bold shadow-[0_4px_20px_-2px_rgba(15,25,74,0.04)] transition-all group';
        const icon = link.querySelector('.material-symbols-outlined');
        if (icon) icon.className = 'material-symbols-outlined text-secondary-fixed';
      } else {
        link.className = 'flex items-center justify-between px-space-md py-space-sm rounded-xl text-on-surface-variant font-label-lg text-label-lg hover:bg-surface-container-high hover:text-on-surface transition-all group';
        const icon = link.querySelector('.material-symbols-outlined');
        if (icon) icon.className = 'material-symbols-outlined text-secondary group-hover:text-primary-container transition-colors';
      }
    });

    // Atualiza dados do usuário logado na UI
    let user = this.getUser();
    if (!user) {
      user = await this.getMe();
    }

    if (user) {
      document.querySelectorAll('.user-display-name').forEach(el => el.textContent = user.nome || 'Leitor Aurora');
      document.querySelectorAll('.user-display-role').forEach(el => {
        const roles = {
          'aluno': 'Leitor Aurora',
          'bibliotecario': 'Bibliotecário',
          'administrador': 'Administrador'
        };
        el.textContent = roles[user.tipo] || user.tipo || 'Membro';
      });
      document.querySelectorAll('.user-display-avatar').forEach(img => {
        if (user.fotoUrl) img.src = user.fotoUrl;
      });
    }

    // Atualização dos badges numéricos na sidebar
    try {
      const [readBooks, wishlist, rentals] = await Promise.all([
        this.getReadBooks().catch(() => []),
        this.getWishlist().catch(() => []),
        this.getMyRentals().catch(() => [])
      ]);

      const activeRentals = rentals.filter(r => r.status === 'ativo');

      // Atualiza badge de Já Lidos
      const readBadge = document.querySelector('nav a[data-path="ja-lidos"] span.rounded-full');
      if (readBadge) readBadge.textContent = readBooks.length;

      // Atualiza badge de Desejos
      const wishBadge = document.querySelector('nav a[data-path="lista-de-desejos"] span.rounded-full');
      if (wishBadge) wishBadge.textContent = wishlist.length;

      // Atualiza badge de Sacola / Aluguéis
      const rentalBadge = document.querySelector('header a[data-path="sacola-de-alugueis"] span.rounded-full');
      if (rentalBadge) rentalBadge.textContent = activeRentals.length;
    } catch (e) {
      console.warn('Erro ao atualizar badges da sidebar:', e);
    }
  }
};

window.AuroraAPI = AuroraAPI;
