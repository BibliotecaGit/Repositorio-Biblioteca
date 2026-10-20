/**
 * Aurora - Cliente de Integração com API REST & Modo Offline para GitHub Pages
 * Suporta conexão com Spring Boot REST API e fallback automático para armazenamento local / demo no GitHub Pages.
 */

const API_BASE = window.AURORA_API_BASE || (
  window.location.protocol === 'https:' || window.location.hostname.includes('github.io')
    ? null
    : 'http://localhost:8080/api'
);

// =============================================================================
// CATÁLOGO OFICIAL DE LIVROS (14 títulos da Spec v3.0 / schema-jules.sql)
// =============================================================================
const DEFAULT_BOOKS = [
  {
    id: 1,
    titulo: 'O Pequeno Príncipe',
    autor: 'Antoine de Saint-Exupéry',
    genero: 'Infantil',
    numPaginas: 96,
    valorLivro: 35.00,
    precoAluguel: 5.00,
    copiasDisponiveis: 3,
    totalCopias: 3,
    disponivel: true,
    capaUrl: 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400&q=80',
    dataLancamento: '2026-07-15',
    avaliacaoMedia: 4.9,
    totalAvaliacoes: 128,
    alugueisUltimoAno: 45,
    ehInfantil: true,
    faixaTamanho: 'pequeno',
    descricao: 'A clássica e poética história da amizade entre um piloto caído no deserto e um jovem príncipe vindo de um asteroide distante.'
  },
  {
    id: 2,
    titulo: 'A Metamorfose',
    autor: 'Franz Kafka',
    genero: 'Ficção Clássica',
    numPaginas: 88,
    valorLivro: 30.00,
    precoAluguel: 4.50,
    copiasDisponiveis: 3,
    totalCopias: 3,
    disponivel: true,
    capaUrl: 'https://images.unsplash.com/photo-1543002588-bfa74002ed7e?w=400&q=80',
    dataLancamento: '2026-01-20',
    avaliacaoMedia: 4.6,
    totalAvaliacoes: 95,
    alugueisUltimoAno: 32,
    ehInfantil: false,
    faixaTamanho: 'pequeno',
    descricao: 'Gregor Samsa acorda certa manhã transformado em um inseto monstruoso, expondo as tensões e fragilidades humanas.'
  },
  {
    id: 3,
    titulo: 'A Revolução dos Bichos',
    autor: 'George Orwell',
    genero: 'Fábula Política',
    numPaginas: 95,
    valorLivro: 32.00,
    precoAluguel: 5.00,
    copiasDisponiveis: 3,
    totalCopias: 3,
    disponivel: true,
    capaUrl: 'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=400&q=80',
    dataLancamento: '2026-06-10',
    avaliacaoMedia: 4.8,
    totalAvaliacoes: 210,
    alugueisUltimoAno: 60,
    ehInfantil: false,
    faixaTamanho: 'pequeno',
    descricao: 'Uma sátira brilhante e contundente sobre o poder, a corrupção e a liberdade na Fazenda dos Animais.'
  },
  {
    id: 4,
    titulo: 'O Alienista',
    autor: 'Machado de Assis',
    genero: 'Clássico Brasileiro',
    numPaginas: 120,
    valorLivro: 28.00,
    precoAluguel: 4.00,
    copiasDisponiveis: 3,
    totalCopias: 3,
    disponivel: true,
    capaUrl: 'https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=400&q=80',
    dataLancamento: '2025-09-12',
    avaliacaoMedia: 4.7,
    totalAvaliacoes: 85,
    alugueisUltimoAno: 29,
    ehInfantil: false,
    faixaTamanho: 'medio_pequeno',
    descricao: 'Dr. Simão Bacamarte funda a Casa Verde em Itaguaí para estudar a loucura, até que a cidade inteira é questionada.'
  },
  {
    id: 5,
    titulo: 'O Velho e o Mar',
    autor: 'Ernest Hemingway',
    genero: 'Drama',
    numPaginas: 128,
    valorLivro: 38.00,
    precoAluguel: 6.00,
    copiasDisponiveis: 3,
    totalCopias: 3,
    disponivel: true,
    capaUrl: 'https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=400&q=80',
    dataLancamento: '2026-04-18',
    avaliacaoMedia: 4.8,
    totalAvaliacoes: 140,
    alugueisUltimoAno: 52,
    ehInfantil: false,
    faixaTamanho: 'medio_pequeno',
    descricao: 'A jornada heróica de Santiago, um velho pescador cubano em sua luta épica contra um marlim gigante no Golfo.'
  },
  {
    id: 6,
    titulo: 'Fahrenheit 451',
    autor: 'Ray Bradbury',
    genero: 'Ficção Científica',
    numPaginas: 144,
    valorLivro: 42.00,
    precoAluguel: 6.50,
    copiasDisponiveis: 3,
    totalCopias: 3,
    disponivel: true,
    capaUrl: 'https://images.unsplash.com/photo-1506880018603-83d5b814b5a6?w=400&q=80',
    dataLancamento: '2026-08-01',
    avaliacaoMedia: 4.7,
    totalAvaliacoes: 115,
    alugueisUltimoAno: 48,
    ehInfantil: false,
    faixaTamanho: 'medio_pequeno',
    descricao: 'Em um futuro distópico, livros são proibidos e bombeiros têm como dever atear fogo em qualquer exemplar encontrado.'
  },
  {
    id: 7,
    titulo: 'Noites Brancas',
    autor: 'Fiódor Dostoiévski',
    genero: 'Romance',
    numPaginas: 112,
    valorLivro: 34.00,
    precoAluguel: 5.00,
    copiasDisponiveis: 3,
    totalCopias: 3,
    disponivel: true,
    // Corrigido: URL perene válida do Unsplash (substitui a photo-1532012164546-f432f2e3777f que dava 404)
    capaUrl: 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400&q=80',
    dataLancamento: '2026-05-22',
    avaliacaoMedia: 4.9,
    totalAvaliacoes: 160,
    alugueisUltimoAno: 58,
    ehInfantil: false,
    faixaTamanho: 'medio_pequeno',
    descricao: 'Durante as noites luminosas de São Petersburgo, um jovem sonhador conhece Nástenka e vive um amor inesquecível de quatro noites.'
  },
  {
    id: 8,
    titulo: 'Dom Casmurro',
    autor: 'Machado de Assis',
    genero: 'Romance Clássico',
    numPaginas: 208,
    valorLivro: 45.00,
    precoAluguel: 7.00,
    copiasDisponiveis: 3,
    totalCopias: 3,
    disponivel: true,
    capaUrl: 'https://images.unsplash.com/photo-1476275466078-4007374efbbe?w=400&q=80',
    dataLancamento: '2024-05-10',
    avaliacaoMedia: 4.7,
    totalAvaliacoes: 310,
    alugueisUltimoAno: 85,
    ehInfantil: false,
    faixaTamanho: 'medio_padrao',
    descricao: 'Bento Santiago narra sua vida, sua paixão por Capitu e as tormentas do ciúme e da dúvida que marcaram a literatura brasileira.'
  },
  {
    id: 9,
    titulo: '1984',
    autor: 'George Orwell',
    genero: 'Ficção Científica',
    numPaginas: 240,
    valorLivro: 48.00,
    precoAluguel: 7.50,
    copiasDisponiveis: 3,
    totalCopias: 3,
    disponivel: true,
    capaUrl: 'https://images.unsplash.com/photo-1495640388908-05fa85288e61?w=400&q=80',
    dataLancamento: '2026-02-14',
    avaliacaoMedia: 4.9,
    totalAvaliacoes: 450,
    alugueisUltimoAno: 110,
    ehInfantil: false,
    faixaTamanho: 'medio_padrao',
    descricao: 'Winston Smith vive sob a vigilância onipresente do Grande Irmão em uma sociedade onde o pensamento livre é o maior dos crimes.'
  },
  {
    id: 10,
    titulo: 'O Carteiro e o Poeta',
    autor: 'Antonio Skármeta',
    genero: 'Romance',
    numPaginas: 168,
    valorLivro: 36.00,
    precoAluguel: 5.50,
    copiasDisponiveis: 3,
    totalCopias: 3,
    disponivel: true,
    capaUrl: 'https://images.unsplash.com/photo-1516979187457-637abb4f9353?w=400&q=80',
    dataLancamento: '2025-11-05',
    avaliacaoMedia: 4.6,
    totalAvaliacoes: 78,
    alugueisUltimoAno: 42,
    ehInfantil: false,
    faixaTamanho: 'medio_padrao',
    descricao: 'Uma amizade delicada e emocionante entre Mario Jiménez, um jovem carteiro humilde de Isla Negra, e o poeta Pablo Neruda.'
  },
  {
    id: 11,
    titulo: 'Vidas Secas',
    autor: 'Graciliano Ramos',
    genero: 'Clássico Brasileiro',
    numPaginas: 176,
    valorLivro: 39.00,
    precoAluguel: 6.00,
    copiasDisponiveis: 3,
    totalCopias: 3,
    disponivel: true,
    capaUrl: 'https://images.unsplash.com/photo-1519682337058-a94d519337bc?w=400&q=80',
    dataLancamento: '2026-03-30',
    avaliacaoMedia: 4.8,
    totalAvaliacoes: 195,
    alugueisUltimoAno: 65,
    ehInfantil: false,
    faixaTamanho: 'medio_padrao',
    descricao: 'A saga dolorosa e comovente da família de Fabiano, Sinha Vitória e seus filhos retirantes pelo sertão nordestino.'
  },
  {
    id: 12,
    titulo: 'Cem Anos de Solidão',
    autor: 'Gabriel García Márquez',
    genero: 'Realismo Mágico',
    numPaginas: 448,
    valorLivro: 65.00,
    precoAluguel: 9.00,
    copiasDisponiveis: 3,
    totalCopias: 3,
    disponivel: true,
    capaUrl: 'https://images.unsplash.com/photo-1463320726281-696a485928c7?w=400&q=80',
    dataLancamento: '2025-08-15',
    avaliacaoMedia: 4.9,
    totalAvaliacoes: 520,
    alugueisUltimoAno: 140,
    ehInfantil: false,
    faixaTamanho: 'grande',
    descricao: 'A extraordinária epopeia das sete gerações da família Buendía na mágica e inesquecível aldeia de Macondo.'
  },
  {
    id: 13,
    titulo: 'Crime e Castigo',
    autor: 'Fiódor Dostoiévski',
    genero: 'Ficção Clássica',
    numPaginas: 592,
    valorLivro: 72.00,
    precoAluguel: 10.00,
    copiasDisponiveis: 3,
    totalCopias: 3,
    disponivel: true,
    capaUrl: 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400&q=80',
    dataLancamento: '2026-04-10',
    avaliacaoMedia: 4.9,
    totalAvaliacoes: 380,
    alugueisUltimoAno: 95,
    ehInfantil: false,
    faixaTamanho: 'grande',
    descricao: 'O jovem estudante Raskólnikov comete um assassinato justificado pela sua teoria de homens superiores, enfrentando a culpa e redenção.'
  },
  {
    id: 14,
    titulo: 'Grande Sertão: Veredas',
    autor: 'João Guimarães Rosa',
    genero: 'Clássico Brasileiro',
    numPaginas: 624,
    valorLivro: 85.00,
    precoAluguel: 11.50,
    copiasDisponiveis: 3,
    totalCopias: 3,
    disponivel: true,
    capaUrl: 'https://images.unsplash.com/photo-1457369804613-52c61a468e7d?w=400&q=80',
    dataLancamento: '2026-06-01',
    avaliacaoMedia: 4.9,
    totalAvaliacoes: 290,
    alugueisUltimoAno: 78,
    ehInfantil: false,
    faixaTamanho: 'grande',
    descricao: 'Riobaldo narra suas aventuras pelo sertão das Minas e da Bahia, sua paixão por Diadorim e suas dúvidas metafísicas sobre o bem e o mal.'
  }
];

// =============================================================================
// MOCK STORE (LOCAL STORAGE) PARA GITHUB PAGES
// =============================================================================
const AuroraMockStore = {
  _get(key, defaultValue) {
    try {
      const data = localStorage.getItem(`aurora_mock_${key}`);
      return data ? JSON.parse(data) : defaultValue;
    } catch (e) {
      return defaultValue;
    }
  },

  _set(key, value) {
    try {
      localStorage.setItem(`aurora_mock_${key}`, JSON.stringify(value));
    } catch (e) {}
  },

  getCurrentUserId() {
    const user = AuroraAPI.getUser();
    return user && user.id ? user.id : 3;
  },

  getBooks() {
    let books = this._get('books', null);
    if (!books || !Array.isArray(books) || books.length === 0) {
      books = JSON.parse(JSON.stringify(DEFAULT_BOOKS));
      this._set('books', books);
    }
    return books;
  },

  saveBooks(books) {
    this._set('books', books);
  },

  getUsers() {
    return this._get('users', [
      { id: 1, nome: 'Admin Aurora', email: 'admin@aurora.local', senha: 'admin123', tipo: 'administrador' },
      { id: 2, nome: 'Bibliotecário Aurora', email: 'biblio@aurora.local', senha: 'biblio123', tipo: 'bibliotecario' },
      { id: 3, nome: 'Helena Souza', email: 'aluno@aurora.local', senha: 'aluno123', tipo: 'aluno' }
    ]);
  },

  // Task 2: Sem aluguéis pré-definidos (conta nova começa com 0 aluguéis)
  getRentals() {
    return this._get('rentals', []);
  },

  saveRentals(rentals) {
    this._set('rentals', rentals);
  },

  // Task 2: Inicializa vazio para novos usuários
  getRead() {
    return this._get('read_books', []);
  },

  saveRead(readList) {
    this._set('read_books', readList);
  },

  getWishlist() {
    return this._get('wishlist', []);
  },

  saveWishlist(wishlist) {
    this._set('wishlist', wishlist);
  },

  getRatings() {
    return this._get('ratings', []);
  },

  saveRatings(ratings) {
    this._set('ratings', ratings);
  },

  // Task 4: Controle de Filas de Espera
  getWaitlist() {
    return this._get('waitlist', {});
  },

  saveWaitlist(w) {
    this._set('waitlist', w);
  },

  getBookQueue(bookId) {
    const w = this.getWaitlist();
    return w[bookId] || [];
  },

  joinQueue(bookId) {
    const w = this.getWaitlist();
    const list = w[bookId] || [];
    const userId = this.getCurrentUserId();
    const user = AuroraAPI.getUser() || {};

    if (list.length >= 10) {
      throw new Error('A fila de espera atingiu o limite de 10 leitores. O livro está com status Pendente.');
    }

    const existingIdx = list.findIndex(i => (typeof i === 'object' ? i.id === userId : i === userId));
    if (existingIdx >= 0) {
      return { position: existingIdx + 1, total: list.length, alreadyInQueue: true };
    }

    list.push({
      id: userId,
      nome: user.nome || 'Leitor Aurora',
      email: user.email || '',
      joinedAt: new Date().toISOString()
    });
    w[bookId] = list;
    this.saveWaitlist(w);
    return { position: list.length, total: list.length, success: true };
  },

  leaveQueue(bookId) {
    const w = this.getWaitlist();
    let list = w[bookId] || [];
    const userId = this.getCurrentUserId();
    list = list.filter(i => (typeof i === 'object' ? i.id !== userId : i !== userId));
    w[bookId] = list;
    this.saveWaitlist(w);
    return { success: true };
  },

  // Roteador central do Mock Store
  async handle(endpoint, options = {}) {
    const method = (options.method || 'GET').toUpperCase();
    const url = endpoint.split('?')[0];
    const params = new URLSearchParams(endpoint.includes('?') ? endpoint.split('?')[1] : '');

    // 1. Auth: Login de Leitor
    if (url === '/auth/login' && method === 'POST') {
      const body = JSON.parse(options.body || '{}');
      const users = this.getUsers();
      const email = (body.email || '').toLowerCase().trim();
      const user = users.find(u => (u.email || '').toLowerCase() === email);
      if (!user || (user.senha && user.senha !== body.senha)) {
        throw new Error('E-mail ou senha incorretos. Tente novamente.');
      }
      const safeUser = { id: user.id, nome: user.nome, email: user.email, tipo: user.tipo || 'aluno', fotoUrl: user.fotoUrl || '' };
      return { token: `mock-token-${Date.now()}`, user: safeUser };
    }

    // 1.1 Auth: Login de Equipe (Admin / Bibliotecário)
    if (url === '/auth/login-staff' && method === 'POST') {
      const body = JSON.parse(options.body || '{}');
      const ident = (body.ident || '').toLowerCase().trim();
      const senha = body.senha;
      const token2fa = (body.token2fa || '').toUpperCase().trim();
      const users = this.getUsers();

      let user = users.find(u => (u.email || '').toLowerCase() === ident || (u.matricula && u.matricula.toLowerCase() === ident));

      if (!user) {
        if (token2fa === 'ADMIN24H' || ident.includes('admin')) {
          user = { id: Date.now(), nome: 'Administrador Aurora', email: ident || 'admin@aurora.local', senha, tipo: 'administrador' };
          users.push(user);
          this._set('users', users);
        } else if (['BIBLIO2026', 'AURORA24', 'STAFF2026'].includes(token2fa) || ident.includes('biblio')) {
          user = { id: Date.now(), nome: 'Bibliotecário Aurora', email: ident || 'biblio@aurora.local', senha, tipo: 'bibliotecario' };
          users.push(user);
          this._set('users', users);
        } else {
          throw new Error('Credenciais de equipe não encontradas ou inválidas.');
        }
      } else {
        if (user.senha && user.senha !== senha) {
          throw new Error('Senha funcional incorreta.');
        }
      }

      const safeUser = { id: user.id, nome: user.nome, email: user.email, tipo: user.tipo || 'bibliotecario', fotoUrl: user.fotoUrl || '' };
      return { token: `mock-staff-token-${Date.now()}`, user: safeUser };
    }

    // 2. Auth: Register
    if (url === '/auth/register' && method === 'POST') {
      const body = JSON.parse(options.body || '{}');
      const users = this.getUsers();
      const email = (body.email || '').toLowerCase().trim();
      if (users.some(u => (u.email || '').toLowerCase() === email)) {
        throw new Error('Este e-mail já está cadastrado no sistema.');
      }

      let tipo = body.tipo || 'aluno';
      const authCode = (body.authCode || '').toUpperCase().trim();
      if (authCode === 'ADMIN24H') {
        tipo = 'administrador';
      } else if (['BIBLIO2026', 'AURORA24', 'STAFF2026'].includes(authCode)) {
        tipo = 'bibliotecario';
      }

      const newUser = {
        id: Date.now(),
        nome: body.nome || 'Novo Leitor',
        email: email,
        senha: body.senha,
        tipo: tipo,
        cep: body.cep || '',
        cidade: body.cidade || '',
        fotoUrl: ''
      };
      users.push(newUser);
      this._set('users', users);
      const safeUser = { id: newUser.id, nome: newUser.nome, email: newUser.email, tipo: newUser.tipo, fotoUrl: '' };
      return { token: `mock-token-${Date.now()}`, user: safeUser };
    }

    // 3. Auth: Me
    if (url === '/auth/me') {
      const user = AuroraAPI.getUser();
      return user || { id: 3, nome: 'Helena Souza', email: 'aluno@aurora.local', tipo: 'aluno' };
    }

    // 4. Books: Catálogo com Filtros Simultâneos Combinados (Task 4)
    if (url === '/books' && method === 'GET') {
      let books = this.getBooks();
      const search = (params.get('search') || '').toLowerCase().trim();
      const tamanho = params.get('tamanho');
      const genero = params.get('genero');
      const autor = params.get('autor');
      const novidades = params.get('novidades') === 'true';
      const destaques = params.get('destaques') === 'true';
      const infantis = params.get('infantis') === 'true';

      if (search) {
        books = books.filter(b => (b.titulo || '').toLowerCase().includes(search) || (b.autor || '').toLowerCase().includes(search));
      }
      if (tamanho && tamanho !== 'all') {
        books = books.filter(b => b.faixaTamanho === tamanho);
      }
      if (genero && genero !== 'all') {
        books = books.filter(b => (b.genero || '').toLowerCase() === genero.toLowerCase());
      }
      if (autor && autor !== 'all') {
        books = books.filter(b => (b.autor || '').toLowerCase() === autor.toLowerCase());
      }
      if (novidades) {
        books = books.slice().sort((a, b) => new Date(b.dataLancamento) - new Date(a.dataLancamento));
      }
      if (destaques) {
        books = books.filter(b => b.avaliacaoMedia >= 4.7);
      }
      if (infantis) {
        books = books.filter(b => b.ehInfantil === true);
      }
      return books;
    }

    if (url === '/books/novidades') {
      const books = this.getBooks();
      return books.slice().sort((a, b) => new Date(b.dataLancamento) - new Date(a.dataLancamento)).slice(0, 4);
    }
    if (url === '/books/destaques') {
      const books = this.getBooks();
      return books.slice().filter(b => b.avaliacaoMedia >= 4.7).slice(0, 4);
    }
    if (url === '/books/infantis') {
      const books = this.getBooks();
      return books.filter(b => b.ehInfantil === true);
    }

    // 6. Book by ID
    const bookMatch = url.match(/^\/books\/(\d+)$/);
    if (bookMatch && method === 'GET') {
      const id = parseInt(bookMatch[1], 10);
      const book = this.getBooks().find(b => b.id === id);
      if (!book) throw new Error('Livro não encontrado');
      return book;
    }

    // 7. Alugar Livro (Task 2: Associado ao userId)
    const rentMatch = url.match(/^\/rentals\/book\/(\d+)$/);
    if (rentMatch && method === 'POST') {
      const idLivro = parseInt(rentMatch[1], 10);
      const userId = this.getCurrentUserId();
      const rentals = this.getRentals();
      const activeCount = rentals.filter(r => (r.userId === userId || !r.userId) && r.status === 'ativo').length;
      if (activeCount >= 3) {
        throw new Error('Limite máximo de 3 aluguéis simultâneos atingido (RN-006).');
      }
      const books = this.getBooks();
      const book = books.find(b => b.id === idLivro);
      if (!book) throw new Error('Livro não encontrado.');
      if (book.copiasDisponiveis <= 0) {
        throw new Error('Exemplar indisponível no momento (0/3 cópias disponíveis).');
      }

      book.copiasDisponiveis--;
      book.disponivel = book.copiasDisponiveis > 0;
      this.saveBooks(books);

      const prazoDias = (book.faixaTamanho === 'pequeno' || book.faixaTamanho === 'medio_pequeno') ? 15 : 30;
      const targetDate = new Date(Date.now() + prazoDias * 86400000).toISOString().split('T')[0];
      const newRental = {
        id: Date.now(),
        userId: userId,
        idLivro: book.id,
        bookId: book.id,
        bookTitle: book.titulo,
        bookCopyId: (3 - book.copiasDisponiveis),
        livro: book,
        dataAluguel: new Date().toISOString().split('T')[0],
        dataPrevistaDevolucao: targetDate,
        dataDevolucaoPrevista: targetDate,
        status: 'ativo',
        extensoes: 0,
        extensaoContador: 0,
        valorAluguel: book.precoAluguel
      };
      rentals.unshift(newRental);
      this.saveRentals(rentals);
      return newRental;
    }

    // 8. Meus Aluguéis (Task 2: Filtra pelo userId)
    if (url === '/rentals/my-rentals' && method === 'GET') {
      const userId = this.getCurrentUserId();
      return this.getRentals().filter(r => r.userId === userId);
    }

    // 9. Estender Aluguel
    const extendMatch = url.match(/^\/rentals\/(\d+)\/extend$/);
    if (extendMatch && method === 'POST') {
      const rentalId = parseInt(extendMatch[1], 10);
      const rentals = this.getRentals();
      const rental = rentals.find(r => r.id === rentalId);
      if (!rental) throw new Error('Aluguel não encontrado.');
      const currentExt = (rental.extensaoContador ?? rental.extensoes ?? 0);
      if (currentExt >= 2) {
        throw new Error('Limite de 2 renovações atingido para este aluguel (RN-001).');
      }
      rental.extensoes = currentExt + 1;
      rental.extensaoContador = rental.extensoes;
      const prevDate = new Date(rental.dataDevolucaoPrevista || rental.dataPrevistaDevolucao).getTime();
      const book = rental.livro || this.getBooks().find(b => b.id === (rental.idLivro || rental.bookId));
      const extraDias = (book && (book.faixaTamanho === 'pequeno' || book.faixaTamanho === 'medio_pequeno')) ? 15 : 30;
      const newDateStr = new Date(prevDate + extraDias * 86400000).toISOString().split('T')[0];
      rental.dataPrevistaDevolucao = newDateStr;
      rental.dataDevolucaoPrevista = newDateStr;
      this.saveRentals(rentals);
      return rental;
    }

    // 10. Devolver Aluguel (Task 2: +1 em copiasDisponiveis imediatamente)
    const returnMatch = url.match(/^\/rentals\/(\d+)\/return$/);
    if (returnMatch && method === 'POST') {
      const rentalId = parseInt(returnMatch[1], 10);
      const rentals = this.getRentals();
      const rental = rentals.find(r => r.id === rentalId);
      if (!rental) throw new Error('Aluguel não encontrado.');
      rental.status = 'devolvido';
      rental.dataDevolucaoEfetiva = new Date().toISOString();
      this.saveRentals(rentals);

      const books = this.getBooks();
      const book = books.find(b => b.id === (rental.idLivro || rental.bookId));
      if (book) {
        book.copiasDisponiveis = Math.min(3, (book.copiasDisponiveis || 0) + 1);
        book.disponivel = true;
        this.saveBooks(books);
      }
      return rental;
    }

    // 10.1 Cancelar Aluguel (Task 2: +1 em copiasDisponiveis imediatamente)
    const cancelMatch = url.match(/^\/rentals\/(\d+)\/cancel$/);
    if (cancelMatch && method === 'POST') {
      const rentalId = parseInt(cancelMatch[1], 10);
      const rentals = this.getRentals();
      const rental = rentals.find(r => r.id === rentalId);
      if (!rental) throw new Error('Aluguel não encontrado.');
      rental.status = 'cancelado';
      this.saveRentals(rentals);

      const books = this.getBooks();
      const book = books.find(b => b.id === (rental.idLivro || rental.bookId));
      if (book) {
        book.copiasDisponiveis = Math.min(3, (book.copiasDisponiveis || 0) + 1);
        book.disponivel = true;
        this.saveBooks(books);
      }
      return rental;
    }

    // 11. Já Lidos (Task 2: associado a userId)
    if (url === '/collections/read') {
      const userId = this.getCurrentUserId();
      return this.getRead().filter(b => b.userId === userId);
    }
    const readMatch = url.match(/^\/collections\/read\/(\d+)$/);
    if (readMatch) {
      const bookId = parseInt(readMatch[1], 10);
      const userId = this.getCurrentUserId();
      let readList = this.getRead();
      if (method === 'POST') {
        const book = this.getBooks().find(b => b.id === bookId);
        if (book && !readList.some(b => b.id === bookId && b.userId === userId)) {
          readList.unshift({ ...book, userId, bookId: book.id });
          this.saveRead(readList);
        }
        return { message: 'Marcado como lido.' };
      }
      if (method === 'DELETE') {
        readList = readList.filter(b => !(b.id === bookId && b.userId === userId));
        this.saveRead(readList);
        return { message: 'Removido de já lidos.' };
      }
    }

    // 12. Lista de Desejos (Task 2: associado a userId)
    if (url === '/collections/wishlist') {
      const userId = this.getCurrentUserId();
      return this.getWishlist().filter(b => b.userId === userId);
    }
    const wishMatch = url.match(/^\/collections\/wishlist\/(\d+)$/);
    if (wishMatch) {
      const bookId = parseInt(wishMatch[1], 10);
      const userId = this.getCurrentUserId();
      let wishList = this.getWishlist();
      if (method === 'POST') {
        const book = this.getBooks().find(b => b.id === bookId);
        if (book && !wishList.some(b => b.id === bookId && b.userId === userId)) {
          wishList.unshift({ ...book, userId, bookId: book.id });
          this.saveWishlist(wishList);
        }
        return { message: 'Adicionado à Lista de Desejos.' };
      }
      if (method === 'DELETE') {
        wishList = wishList.filter(b => !(b.id === bookId && b.userId === userId));
        this.saveWishlist(wishList);
        return { message: 'Removido da Lista de Desejos.' };
      }
    }

    // 13. Avaliações (Task 5: criar, editar, excluir, exibir título e comentário)
    const rateMatch = url.match(/^\/collections\/ratings\/book\/(\d+)$/);
    if (rateMatch && method === 'POST') {
      const bookId = parseInt(rateMatch[1], 10);
      const userId = this.getCurrentUserId();
      const body = JSON.parse(options.body || '{}');
      let ratings = this.getRatings();
      const books = this.getBooks();
      const book = books.find(b => b.id === bookId);
      const bookTitle = book ? book.titulo : `Livro #${bookId}`;

      const existingIdx = ratings.findIndex(r => (r.bookId === bookId || r.idLivro === bookId) && r.userId === userId);
      if (existingIdx >= 0) {
        ratings[existingIdx].nota = Number(body.nota || 5);
        ratings[existingIdx].comentario = body.comentario || '';
        ratings[existingIdx].bookTitle = bookTitle;
        ratings[existingIdx].dataAvaliacao = new Date().toISOString();
      } else {
        ratings.unshift({
          id: Date.now(),
          userId: userId,
          idLivro: bookId,
          bookId: bookId,
          bookTitle: bookTitle,
          autor: book ? book.autor : '',
          capaUrl: book ? book.capaUrl : '',
          nota: Number(body.nota || 5),
          comentario: body.comentario || '',
          dataCriacao: new Date().toISOString(),
          dataAvaliacao: new Date().toISOString()
        });
      }
      this.saveRatings(ratings);
      return { message: 'Avaliação registrada com sucesso!' };
    }
    if (rateMatch && method === 'DELETE') {
      const bookId = parseInt(rateMatch[1], 10);
      const userId = this.getCurrentUserId();
      let ratings = this.getRatings();
      ratings = ratings.filter(r => !((r.bookId === bookId || r.idLivro === bookId) && r.userId === userId));
      this.saveRatings(ratings);
      return { message: 'Avaliação excluída com sucesso.' };
    }
    if (url === '/collections/ratings/my-ratings') {
      const userId = this.getCurrentUserId();
      const ratings = this.getRatings().filter(r => r.userId === userId);
      const books = this.getBooks();
      return ratings.map(r => {
        const b = books.find(book => book.id === (r.bookId || r.idLivro));
        return {
          ...r,
          bookTitle: b ? b.titulo : (r.bookTitle || `Livro #${r.bookId}`),
          autor: b ? b.autor : (r.autor || ''),
          capaUrl: b ? b.capaUrl : (r.capaUrl || '')
        };
      });
    }

    // 14. Sugestões
    if (url === '/suggestions') {
      const books = this.getBooks();
      const list = [
        ...books.slice().sort((a, b) => (b.alugueisUltimoAno || 0) - (a.alugueisUltimoAno || 0)).slice(0, 4),
        ...books.slice().sort((a, b) => new Date(b.dataLancamento) - new Date(a.dataLancamento)).slice(0, 4)
      ];
      const unique = [];
      const seen = new Set();
      for (const b of list) {
        if (!seen.has(b.id)) {
          seen.add(b.id);
          unique.push(b);
        }
      }
      return unique;
    }
    if (url.startsWith('/suggestions/record-filter')) {
      return { message: 'Filtro registrado.' };
    }

    // 15. Multas
    if (url === '/fines/my-fines') {
      return [];
    }

    // 16. Fila & Pendente (Task 4)
    const queueMatch = url.match(/^\/queue\/book\/(\d+)$/);
    if (queueMatch && method === 'GET') {
      const bookId = parseInt(queueMatch[1], 10);
      const q = this.getBookQueue(bookId);
      const userId = this.getCurrentUserId();
      const pos = q.findIndex(i => (typeof i === 'object' ? i.id === userId : i === userId));
      return {
        bookId,
        total: q.length,
        position: pos >= 0 ? pos + 1 : null,
        isFull: q.length >= 10,
        inQueue: pos >= 0
      };
    }
    const joinMatch = url.match(/^\/queue\/book\/(\d+)\/join$/);
    if (joinMatch && method === 'POST') {
      const bookId = parseInt(joinMatch[1], 10);
      return this.joinQueue(bookId);
    }
    const leaveMatch = url.match(/^\/queue\/book\/(\d+)\/leave$/);
    if (leaveMatch && method === 'POST') {
      const bookId = parseInt(leaveMatch[1], 10);
      return this.leaveQueue(bookId);
    }

    // 17. Perfil do Usuário (Task 6)
    if (url === '/user/profile') {
      const users = this.getUsers();
      const currentId = this.getCurrentUserId();
      let current = users.find(u => u.id === currentId) || AuroraAPI.getUser() || {};

      if (method === 'PUT') {
        const body = JSON.parse(options.body || '{}');
        const updated = { ...current, ...body };
        if (body.endereco) {
          updated.endereco = { ...(current.endereco || {}), ...body.endereco };
        }
        const idx = users.findIndex(u => u.id === currentId);
        if (idx >= 0) {
          users[idx] = updated;
          this._set('users', users);
        }
        AuroraAPI.setUser(updated);
        return updated;
      }
      return current;
    }

    return {};
  }
};

// =============================================================================
// OBJETO CENTRAL DA API AURORA
// =============================================================================
const AuroraAPI = {
  getToken() {
    return localStorage.getItem('aurora_token') || sessionStorage.getItem('aurora_token');
  },

  setToken(token, remember = true) {
    if (remember) {
      localStorage.setItem('aurora_token', token);
    } else {
      sessionStorage.setItem('aurora_token', token);
    }
  },

  removeToken() {
    localStorage.removeItem('aurora_token');
    sessionStorage.removeItem('aurora_token');
    localStorage.removeItem('aurora_user');
    sessionStorage.removeItem('aurora_user');
  },

  getUser() {
    try {
      const u = localStorage.getItem('aurora_user') || sessionStorage.getItem('aurora_user');
      return u ? JSON.parse(u) : null;
    } catch (e) {
      return null;
    }
  },

  setUser(user, remember = true) {
    try {
      const str = JSON.stringify(user);
      if (remember) {
        localStorage.setItem('aurora_user', str);
      } else {
        sessionStorage.setItem('aurora_user', str);
      }
    } catch (e) {}
  },

  isAuthenticated() {
    return !!this.getToken();
  },

  async apiFetch(endpoint, options = {}) {
    if (!API_BASE) {
      return await AuroraMockStore.handle(endpoint, options);
    }

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
      const response = await fetch(url, { ...options, headers });
      if (response.status === 401) {
        this.removeToken();
        window.location.href = 'login.html?expired=true';
        throw new Error('Sessão expirada. Por favor, autentique-se novamente.');
      }
      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `Erro HTTP ${response.status}: ${response.statusText}`);
      }
      const text = await response.text();
      return text ? JSON.parse(text) : {};
    } catch (err) {
      // Fallback gracioso para Mock Store se o backend estiver inacessível
      if (err.name === 'TypeError' || err.message.includes('Failed to fetch')) {
        console.warn(`[Aurora] Backend em ${API_BASE} inacessível. Chaveando para Mock Store:`, endpoint);
        return await AuroraMockStore.handle(endpoint, options);
      }
      throw err;
    }
  },

  // ---------------------------------------------------------------------------
  // Métodos de Autenticação
  // ---------------------------------------------------------------------------
  async register(nome, email, senha, remember = true, extra = {}) {
    const data = await this.apiFetch('/auth/register', {
      method: 'POST',
      body: JSON.stringify({ nome, email, senha, ...extra })
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

  async loginStaff(ident, senha, token2fa = '', remember = true) {
    const data = await this.apiFetch('/auth/login-staff', {
      method: 'POST',
      body: JSON.stringify({ ident, senha, token2fa })
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
      if (user) this.setUser(user);
      return user;
    } catch (e) {
      return this.getUser();
    }
  },

  logout() {
    this.removeToken();
    window.location.href = 'login.html';
  },

  // ---------------------------------------------------------------------------
  // Catálogo e Filtros de Livros (Task 4)
  // ---------------------------------------------------------------------------
  async getBooks(filters = {}) {
    const params = new URLSearchParams();
    if (filters.search) params.append('search', filters.search);
    if (filters.tamanho && filters.tamanho !== 'all') params.append('tamanho', filters.tamanho);
    if (filters.genero && filters.genero !== 'all') params.append('genero', filters.genero);
    if (filters.autor && filters.autor !== 'all') params.append('autor', filters.autor);
    if (filters.novidades) params.append('novidades', 'true');
    if (filters.destaques) params.append('destaques', 'true');
    if (filters.infantis) params.append('infantis', 'true');

    const qs = params.toString();
    return await this.apiFetch(`/books${qs ? `?${qs}` : ''}`);
  },

  async getBookById(id) {
    return await this.apiFetch(`/books/${id}`);
  },

  // ---------------------------------------------------------------------------
  // Aluguéis e Extensões
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

  async cancelRental(rentalId) {
    return await this.apiFetch(`/rentals/${rentalId}/cancel`, {
      method: 'POST'
    });
  },

  // ---------------------------------------------------------------------------
  // Fila & Pendente (Task 4)
  // ---------------------------------------------------------------------------
  async getBookQueue(bookId) {
    return await this.apiFetch(`/queue/book/${bookId}`);
  },

  async joinQueue(bookId) {
    return await this.apiFetch(`/queue/book/${bookId}/join`, {
      method: 'POST'
    });
  },

  async leaveQueue(bookId) {
    return await this.apiFetch(`/queue/book/${bookId}/leave`, {
      method: 'POST'
    });
  },

  // ---------------------------------------------------------------------------
  // Coleções (Já Lidos, Desejos, Avaliações)
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

  async deleteRating(bookId) {
    return await this.apiFetch(`/collections/ratings/book/${bookId}`, {
      method: 'DELETE'
    });
  },

  async getMyRatings() {
    return await this.apiFetch('/collections/ratings/my-ratings');
  },

  // ---------------------------------------------------------------------------
  // Sugestões e Perfil
  // ---------------------------------------------------------------------------
  async getSuggestions() {
    const res = await this.apiFetch('/suggestions');
    if (Array.isArray(res)) return res;
    if (res && res.populares) {
      return [...(res.populares || []), ...(res.destaques || [])];
    }
    return [];
  },

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
  // Notificações Toast
  // ---------------------------------------------------------------------------
  showToast(message, icon = 'info', isError = false) {
    let toast = document.getElementById('toastNotification');
    if (!toast) {
      toast = document.createElement('div');
      toast.id = 'toastNotification';
      toast.className = 'fixed bottom-6 right-6 z-50 transform translate-y-24 opacity-0 transition-all duration-300 flex items-center gap-3 px-4 py-3 rounded-2xl shadow-xl backdrop-blur-md';
      document.body.appendChild(toast);
    }
    toast.className = `fixed bottom-6 right-6 z-50 transform translate-y-0 opacity-100 transition-all duration-300 flex items-center gap-3 px-4 py-3 rounded-2xl shadow-xl backdrop-blur-md ${
      isError ? 'bg-error-container text-on-error-container border border-error/20' : 'bg-primary-container text-on-primary'
    }`;
    toast.innerHTML = `
      <span class="material-symbols-outlined ${isError ? 'text-error' : 'text-secondary-fixed'} text-[22px]" translate="no">${icon}</span>
      <span class="font-body-sm text-sm font-semibold">${message}</span>
    `;
    clearTimeout(toast._timeout);
    toast._timeout = setTimeout(() => {
      toast.classList.remove('translate-y-0', 'opacity-100');
      toast.classList.add('translate-y-24', 'opacity-0');
    }, 3200);
  },

  // ---------------------------------------------------------------------------
  // Task 3: Sidebar Dinâmica por Cargo com Proteção de Rotas
  // ---------------------------------------------------------------------------
  async initSidebar(activePath = '') {
    const isLoginPage = window.location.pathname.endsWith('login.html');
    if (!this.isAuthenticated()) {
      if (!isLoginPage) {
        window.location.href = 'login.html';
        return;
      }
      return;
    } else if (isLoginPage) {
      const user = this.getUser();
      if (user && user.tipo === 'administrador') window.location.href = 'cadastros.html';
      else if (user && user.tipo === 'bibliotecario') window.location.href = 'estoque.html';
      else window.location.href = 'index.html';
      return;
    }

    let user = this.getUser();
    if (!user) {
      user = await this.getMe();
    }
    const role = (user && user.tipo) ? user.tipo : 'aluno';

    // Bloqueio de acesso por URL para páginas restritas
    const currentPage = window.location.pathname.split('/').pop() || 'index.html';
    if (role === 'aluno') {
      if (['cadastros.html', 'estoque.html', 'reestoque.html'].includes(currentPage)) {
        window.location.href = 'index.html';
        return;
      }
    } else if (role === 'bibliotecario') {
      if (['cadastros.html'].includes(currentPage)) {
        window.location.href = 'estoque.html';
        return;
      }
    }

    // Configuração dos itens da Sidebar conforme cargo (Task 3)
    const nav = document.querySelector('aside nav');
    if (nav) {
      let navItems = [];
      if (role === 'administrador') {
        navItems = [
          { path: 'inicio', href: 'index.html', label: 'Início', icon: 'auto_stories' },
          { path: 'estoque-de-livros', href: 'estoque.html', label: 'Estoque de Livros', icon: 'inventory_2' },
          { path: 'cadastros', href: 'cadastros.html', label: 'Cadastros', icon: 'manage_accounts' },
          { path: 'configuracoes', href: 'configuracoes.html', label: 'Configurações', icon: 'settings' }
        ];
      } else if (role === 'bibliotecario') {
        navItems = [
          { path: 'inicio', href: 'index.html', label: 'Início', icon: 'auto_stories' },
          { path: 'estoque-de-livros', href: 'estoque.html', label: 'Estoque de Livros', icon: 'inventory_2' },
          { path: 'reestoque', href: 'reestoque.html', label: 'Reestoque', icon: 'add_box' },
          { path: 'configuracoes', href: 'configuracoes.html', label: 'Configurações', icon: 'settings' }
        ];
      } else {
        // Aluno / Leitor
        navItems = [
          { path: 'inicio', href: 'index.html', label: 'Início', icon: 'auto_stories' },
          { path: 'prateleira', href: 'prateleira.html', label: 'Prateleira', icon: 'shelves' },
          { path: 'sugestoes', href: 'sugestoes.html', label: 'Sugestões', icon: 'recommend' },
          { path: 'ja-lidos', href: 'ja-lidos.html', label: 'Já Lidos', icon: 'bookmarks', badge: 'read' },
          { path: 'lista-de-desejos', href: 'desejos.html', label: 'Lista de Desejos', icon: 'favorite', badge: 'wish' },
          { path: 'avaliar-livros', href: 'avaliar.html', label: 'Avaliar Livros', icon: 'rate_review' },
          { path: 'configuracoes', href: 'configuracoes.html', label: 'Configurações', icon: 'settings' }
        ];
      }

      nav.innerHTML = navItems.map(item => {
        const isActive = item.path === activePath;
        const activeCls = isActive
          ? 'bg-primary-container text-on-primary font-bold shadow-sm'
          : 'text-on-surface-variant hover:bg-surface-container-high hover:text-on-surface';
        const iconCls = isActive ? 'text-secondary-fixed' : 'text-secondary';
        const badgeSpan = item.badge ? `<span id="badge-${item.badge}" class="px-2 py-0.5 rounded-full bg-secondary-fixed text-on-secondary-fixed text-xs font-bold">0</span>` : '';

        return `
          <a class="flex items-center justify-between px-space-md py-space-sm rounded-xl font-label-lg transition-all group ${activeCls}" data-path="${item.path}" href="${item.href}">
            <div class="flex items-center gap-space-sm">
              <span class="material-symbols-outlined ${iconCls}" translate="no">${item.icon}</span>
              <span>${item.label}</span>
            </div>
            ${badgeSpan}
          </a>
        `;
      }).join('');
    }

    // Configura botões de logout
    document.querySelectorAll('[data-path="login"], .logout-btn').forEach(btn => {
      btn.href = '#';
      btn.addEventListener('click', (e) => {
        e.preventDefault();
        this.logout();
      });
    });

    // Atualiza nome, cargo e avatar na interface
    if (user) {
      document.querySelectorAll('.user-display-name').forEach(el => el.textContent = user.nome || 'Helena Souza');
      document.querySelectorAll('aside span').forEach(el => {
        if (el.textContent.trim() === 'Helena Souza' || el.textContent.trim() === 'Helena V.') {
          el.textContent = user.nome;
        }
      });
      document.querySelectorAll('.user-display-role').forEach(el => {
        const roles = {
          'aluno': 'Leitor Aurora',
          'bibliotecario': 'Bibliotecário',
          'administrador': 'Administrador'
        };
        el.textContent = roles[user.tipo] || user.tipo || 'Leitor Aurora';
      });

      const defaultAvatar = user.tipo === 'administrador' ? 'img/avatar-admin.svg' : user.tipo === 'bibliotecario' ? 'img/avatar-biblio.svg' : 'img/avatar-default.svg';
      document.querySelectorAll('.user-display-avatar, #sidebar-avatar, #card-avatar').forEach(img => {
        img.src = user.fotoUrl || defaultAvatar;
      });
    }

    // Tratamento de imagens quebradas (Task 7)
    document.querySelectorAll('img').forEach(img => {
      img.addEventListener('error', function() {
        if (!this.getAttribute('data-error-handled')) {
          this.setAttribute('data-error-handled', 'true');
          const isAvatar = this.classList.contains('rounded-full') || (this.alt && (this.alt.toLowerCase().includes('avatar') || this.alt.toLowerCase().includes('profile')));
          if (isAvatar) {
            this.src = (user && user.tipo === 'administrador') ? 'img/avatar-admin.svg' : 'img/avatar-default.svg';
          } else {
            this.src = 'img/logo.svg';
          }
        }
      });
    });

    // Atualização dos badges numéricos na sidebar (para o leitor)
    if (role === 'aluno') {
      try {
        const [readBooks, wishlist, rentals] = await Promise.all([
          this.getReadBooks().catch(() => []),
          this.getWishlist().catch(() => []),
          this.getMyRentals().catch(() => [])
        ]);

        const activeRentals = (rentals || []).filter(r => r.status === 'ativo');

        const readBadge = document.getElementById('badge-read');
        if (readBadge) readBadge.textContent = readBooks.length;

        const wishBadge = document.getElementById('badge-wish');
        if (wishBadge) wishBadge.textContent = wishlist.length;

        const rentalBadge = document.querySelector('header a[data-path="sacola-de-alugueis"] span.rounded-full');
        if (rentalBadge) rentalBadge.textContent = activeRentals.length;
      } catch (e) {
        console.warn('Erro ao atualizar badges da sidebar:', e);
      }
    }
  }
};

window.AuroraAPI = AuroraAPI;
