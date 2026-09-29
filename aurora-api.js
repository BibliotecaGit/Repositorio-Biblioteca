/**
 * Aurora - Cliente de Integração com API REST & Modo Offline para GitHub Pages
 * Suporta conexão com Spring Boot REST API e fallback automático para armazenamento local / demo no GitHub Pages.
 */

const API_BASE = window.AURORA_API_BASE || (window.location.protocol === 'https:' ? null : 'http://localhost:8080/api');

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
    copiasDisponiveis: 2,
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
    copiasDisponiveis: 2,
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
    capaUrl: 'https://images.unsplash.com/photo-1532012164546-f432f2e3777f?w=400&q=80',
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
    copiasDisponiveis: 2,
    totalCopias: 3,
    disponivel: true,
    capaUrl: 'https://images.unsplash.com/photo-1476275466078-4007374efbbe?w=400&q=80',
    dataLancamento: '2024-05-10',
    avaliacaoMedia: 4.7,
    totalAvaliacoes: 310,
    alugueisUltimoAno: 85,
    ehInfantil: false,
    faixaTamanho: 'medio_padrao',
    descricao: 'Bento Santiago relembra sua juventude, sua paixão por Capitu dos olhos de ressaca e as dúvidas que atormentaram sua vida.'
  },
  {
    id: 9,
    titulo: 'O Hobbit',
    autor: 'J.R.R. Tolkien',
    genero: 'Fantasia',
    numPaginas: 240,
    valorLivro: 55.00,
    precoAluguel: 8.50,
    copiasDisponiveis: 3,
    totalCopias: 3,
    disponivel: true,
    capaUrl: 'https://images.unsplash.com/photo-1629992101753-56d196c8aabb?w=400&q=80',
    dataLancamento: '2026-06-30',
    avaliacaoMedia: 4.9,
    totalAvaliacoes: 420,
    alugueisUltimoAno: 110,
    ehInfantil: false,
    faixaTamanho: 'medio_padrao',
    descricao: 'Bilbo Bolseiro é arrastado para uma aventura fantástica com Gandalf e treze anões para recuperar o tesouro de Smaug.'
  },
  {
    id: 10,
    titulo: 'Admirável Mundo Novo',
    autor: 'Aldous Huxley',
    genero: 'Ficção Científica',
    numPaginas: 224,
    valorLivro: 48.00,
    precoAluguel: 7.50,
    copiasDisponiveis: 3,
    totalCopias: 3,
    disponivel: true,
    capaUrl: 'https://images.unsplash.com/photo-1589829085413-56de8ae18c73?w=400&q=80',
    dataLancamento: '2026-02-14',
    avaliacaoMedia: 4.6,
    totalAvaliacoes: 175,
    alugueisUltimoAno: 42,
    ehInfantil: false,
    faixaTamanho: 'medio_padrao',
    descricao: 'Uma sociedade totalitária tecnologicamente avançada onde os humanos são condicionados biologicamente desde o nascimento.'
  },
  {
    id: 11,
    titulo: 'O Meu Pé de Laranja Lima',
    autor: 'José Mauro de Vasconcelos',
    genero: 'Infantil',
    numPaginas: 192,
    valorLivro: 40.00,
    precoAluguel: 6.00,
    copiasDisponiveis: 3,
    totalCopias: 3,
    disponivel: true,
    capaUrl: 'https://images.unsplash.com/photo-1516979187457-637abb4f9353?w=400&q=80',
    dataLancamento: '2026-07-28',
    avaliacaoMedia: 4.8,
    totalAvaliacoes: 190,
    alugueisUltimoAno: 65,
    ehInfantil: true,
    faixaTamanho: 'medio_padrao',
    descricao: 'A comovente história do menino Zezé, sua imaginação vívida e a amizade pura com seu pé de laranja-lima e o Portuga.'
  },
  {
    id: 12,
    titulo: '1984',
    autor: 'George Orwell',
    genero: 'Ficção Científica',
    numPaginas: 328,
    valorLivro: 55.00,
    precoAluguel: 9.00,
    copiasDisponiveis: 3,
    totalCopias: 3,
    disponivel: true,
    capaUrl: 'https://images.unsplash.com/photo-1541963463532-d68292c34b19?w=400&q=80',
    dataLancamento: '2025-08-15',
    avaliacaoMedia: 4.9,
    totalAvaliacoes: 520,
    alugueisUltimoAno: 140,
    ehInfantil: false,
    faixaTamanho: 'grande',
    descricao: 'Winston Smith vive sob a vigilância onipresente do Grande Irmão na Oceania, onde pensar contra o Partido é crime supremo.'
  },
  {
    id: 13,
    titulo: 'Cem Anos de Solidão',
    autor: 'Gabriel García Márquez',
    genero: 'Realismo Mágico',
    numPaginas: 448,
    valorLivro: 65.00,
    precoAluguel: 10.00,
    copiasDisponiveis: 3,
    totalCopias: 3,
    disponivel: true,
    capaUrl: 'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=400&q=80',
    dataLancamento: '2026-05-05',
    avaliacaoMedia: 4.9,
    totalAvaliacoes: 380,
    alugueisUltimoAno: 95,
    ehInfantil: false,
    faixaTamanho: 'grande',
    descricao: 'A saga de sete gerações da família Buendía na mítica aldeia de Macondo, obra-prima do realismo mágico latino-americano.'
  },
  {
    id: 14,
    titulo: 'Orgulho e Preconceito',
    autor: 'Jane Austen',
    genero: 'Romance',
    numPaginas: 360,
    valorLivro: 50.00,
    precoAluguel: 8.00,
    copiasDisponiveis: 3,
    totalCopias: 3,
    disponivel: true,
    capaUrl: 'https://images.unsplash.com/photo-1463320726281-696a485928c7?w=400&q=80',
    dataLancamento: '2026-03-25',
    avaliacaoMedia: 4.8,
    totalAvaliacoes: 290,
    alugueisUltimoAno: 78,
    ehInfantil: false,
    faixaTamanho: 'grande',
    descricao: 'O clássico embate de inteligência e sentimentos entre a vibrante Elizabeth Bennet e o orgulhoso Sr. Darcy na Inglaterra rural.'
  }
];

// =============================================================================
// MOCK STORE OFFLINE / GITHUB PAGES (Persistência no localStorage)
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

  getRentals() {
    let rentals = this._get('rentals', null);
    if (!rentals) {
      // Seed inicial de 1 aluguel ativo para demonstração acolhedora
      const book = DEFAULT_BOOKS.find(b => b.id === 8) || DEFAULT_BOOKS[0];
      const prazoDias = (book.faixaTamanho === 'pequeno' || book.faixaTamanho === 'medio_pequeno') ? 15 : 30;
      const now = Date.now();
      rentals = [
        {
          id: 101,
          idLivro: book.id,
          livro: book,
          dataAluguel: new Date(now - 3 * 86400000).toISOString(),
          dataPrevistaDevolucao: new Date(now + (prazoDias - 3) * 86400000).toISOString(),
          status: 'ativo',
          extensoes: 0,
          valorAluguel: book.precoAluguel
        }
      ];
      this._set('rentals', rentals);
    }
    return rentals;
  },

  saveRentals(rentals) {
    this._set('rentals', rentals);
  },

  getRead() {
    return this._get('read_books', [
      DEFAULT_BOOKS.find(b => b.id === 1) || DEFAULT_BOOKS[0]
    ]);
  },

  saveRead(readList) {
    this._set('read_books', readList);
  },

  getWishlist() {
    return this._get('wishlist', [
      DEFAULT_BOOKS.find(b => b.id === 9) || DEFAULT_BOOKS[1]
    ]);
  },

  saveWishlist(wishlist) {
    this._set('wishlist', wishlist);
  },

  getRatings() {
    return this._get('ratings', [
      { id: 1, idLivro: 1, nota: 5, comentario: 'Uma das obras mais doces e reflexivas já escritas.', dataCriacao: new Date().toISOString() }
    ]);
  },

  saveRatings(ratings) {
    this._set('ratings', ratings);
  },

  // Roteador central do Mock Store
  async handle(endpoint, options = {}) {
    const method = (options.method || 'GET').toUpperCase();
    const url = endpoint.split('?')[0];
    const params = new URLSearchParams(endpoint.includes('?') ? endpoint.split('?')[1] : '');

    // 1. Auth: Login
    if (url === '/auth/login' && method === 'POST') {
      const body = JSON.parse(options.body || '{}');
      const users = this.getUsers();
      const user = users.find(u => u.email.toLowerCase() === (body.email || '').toLowerCase().trim());
      if (!user || (user.senha && user.senha !== body.senha)) {
        throw new Error('E-mail ou senha incorretos. Tente novamente.');
      }
      const safeUser = { id: user.id, nome: user.nome, email: user.email, tipo: user.tipo };
      return { token: `mock-token-${Date.now()}`, user: safeUser };
    }

    // 2. Auth: Register
    if (url === '/auth/register' && method === 'POST') {
      const body = JSON.parse(options.body || '{}');
      const users = this.getUsers();
      if (users.some(u => u.email.toLowerCase() === (body.email || '').toLowerCase().trim())) {
        throw new Error('Este e-mail já está cadastrado no sistema.');
      }
      const newUser = {
        id: Date.now(),
        nome: body.nome || 'Novo Leitor',
        email: body.email.toLowerCase().trim(),
        senha: body.senha,
        tipo: 'aluno'
      };
      users.push(newUser);
      this._set('users', users);
      const safeUser = { id: newUser.id, nome: newUser.nome, email: newUser.email, tipo: newUser.tipo };
      return { token: `mock-token-${Date.now()}`, user: safeUser };
    }

    // 3. Auth: Me
    if (url === '/auth/me') {
      const user = AuroraAPI.getUser();
      return user || { id: 3, nome: 'Helena Souza', email: 'aluno@aurora.local', tipo: 'aluno' };
    }

    // 4. Books: Catálogo com Filtros
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
      if (genero) {
        books = books.filter(b => (b.genero || '').toLowerCase() === genero.toLowerCase());
      }
      if (autor) {
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

    // 5. Novidades / Destaques / Infantis
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

    // 7. Alugar Livro
    const rentMatch = url.match(/^\/rentals\/book\/(\d+)$/);
    if (rentMatch && method === 'POST') {
      const idLivro = parseInt(rentMatch[1], 10);
      const rentals = this.getRentals();
      const activeCount = rentals.filter(r => r.status === 'ativo').length;
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
      const newRental = {
        id: Date.now(),
        idLivro: book.id,
        livro: book,
        dataAluguel: new Date().toISOString(),
        dataPrevistaDevolucao: new Date(Date.now() + prazoDias * 86400000).toISOString(),
        status: 'ativo',
        extensoes: 0,
        valorAluguel: book.precoAluguel
      };
      rentals.unshift(newRental);
      this.saveRentals(rentals);
      return newRental;
    }

    // 8. Meus Aluguéis
    if (url === '/rentals/my-rentals' && method === 'GET') {
      return this.getRentals();
    }

    // 9. Estender Aluguel
    const extendMatch = url.match(/^\/rentals\/(\d+)\/extend$/);
    if (extendMatch && method === 'POST') {
      const rentalId = parseInt(extendMatch[1], 10);
      const rentals = this.getRentals();
      const rental = rentals.find(r => r.id === rentalId);
      if (!rental) throw new Error('Aluguel não encontrado.');
      if (rental.extensoes >= 2) {
        throw new Error('Limite de 2 renovações atingido para este aluguel (RN-001).');
      }
      rental.extensoes = (rental.extensoes || 0) + 1;
      const prevDate = new Date(rental.dataPrevistaDevolucao).getTime();
      const extraDias = (rental.livro && (rental.livro.faixaTamanho === 'pequeno' || rental.livro.faixaTamanho === 'medio_pequeno')) ? 15 : 30;
      rental.dataPrevistaDevolucao = new Date(prevDate + extraDias * 86400000).toISOString();
      this.saveRentals(rentals);
      return rental;
    }

    // 10. Devolver Aluguel
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
      const book = books.find(b => b.id === rental.idLivro);
      if (book) {
        book.copiasDisponiveis = Math.min(3, (book.copiasDisponiveis || 0) + 1);
        book.disponivel = true;
        this.saveBooks(books);
      }
      return rental;
    }

    // 11. Já Lidos
    if (url === '/collections/read') {
      return this.getRead();
    }
    const readMatch = url.match(/^\/collections\/read\/(\d+)$/);
    if (readMatch) {
      const bookId = parseInt(readMatch[1], 10);
      let readList = this.getRead();
      if (method === 'POST') {
        const book = this.getBooks().find(b => b.id === bookId);
        if (book && !readList.some(b => b.id === bookId)) {
          readList.unshift(book);
          this.saveRead(readList);
        }
        return { message: 'Marcado como lido.' };
      }
      if (method === 'DELETE') {
        readList = readList.filter(b => b.id !== bookId);
        this.saveRead(readList);
        return { message: 'Removido de já lidos.' };
      }
    }

    // 12. Lista de Desejos
    if (url === '/collections/wishlist') {
      return this.getWishlist();
    }
    const wishMatch = url.match(/^\/collections\/wishlist\/(\d+)$/);
    if (wishMatch) {
      const bookId = parseInt(wishMatch[1], 10);
      let wishList = this.getWishlist();
      if (method === 'POST') {
        const book = this.getBooks().find(b => b.id === bookId);
        if (book && !wishList.some(b => b.id === bookId)) {
          wishList.unshift(book);
          this.saveWishlist(wishList);
        }
        return { message: 'Adicionado à Lista de Desejos.' };
      }
      if (method === 'DELETE') {
        wishList = wishList.filter(b => b.id !== bookId);
        this.saveWishlist(wishList);
        return { message: 'Removido da Lista de Desejos.' };
      }
    }

    // 13. Avaliações
    const rateMatch = url.match(/^\/collections\/ratings\/book\/(\d+)$/);
    if (rateMatch && method === 'POST') {
      const bookId = parseInt(rateMatch[1], 10);
      const body = JSON.parse(options.body || '{}');
      const ratings = this.getRatings();
      ratings.unshift({
        id: Date.now(),
        idLivro: bookId,
        nota: Number(body.nota || 5),
        comentario: body.comentario || '',
        dataCriacao: new Date().toISOString()
      });
      this.saveRatings(ratings);
      return { message: 'Avaliação enviada com sucesso!' };
    }
    if (url === '/collections/ratings/my-ratings') {
      return this.getRatings();
    }

    // 14. Sugestões
    if (url === '/suggestions') {
      const books = this.getBooks();
      return {
        populares: books.slice().sort((a, b) => (b.alugueisUltimoAno || 0) - (a.alugueisUltimoAno || 0)).slice(0, 4),
        recentes: books.slice().sort((a, b) => new Date(b.dataLancamento) - new Date(a.dataLancamento)).slice(0, 4),
        destaques: books.filter(b => b.avaliacaoMedia >= 4.8).slice(0, 4)
      };
    }
    if (url.startsWith('/suggestions/record-filter')) {
      return { message: 'Filtro registrado.' };
    }

    // 15. Multas
    if (url === '/fines/my-fines') {
      return [];
    }

    // 16. Perfil do Usuário
    if (url === '/user/profile') {
      if (method === 'PUT') {
        const body = JSON.parse(options.body || '{}');
        const user = AuroraAPI.getUser() || {};
        const updated = { ...user, ...body };
        AuroraAPI.setUser(updated);
        return updated;
      }
      return AuroraAPI.getUser() || {
        id: 3,
        nome: 'Helena Souza',
        email: 'aluno@aurora.local',
        tipo: 'aluno',
        cidade: 'São Paulo',
        uf: 'SP',
        cep: '01310-100',
        logradouro: 'Av. Paulista',
        numero: '1000'
      };
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
  // Fetch Centralizado com Fallback Gracioso para Mock Store
  // ---------------------------------------------------------------------------
  async apiFetch(endpoint, options = {}) {
    // Se não há API_BASE configurada (ex: GitHub Pages rodando em HTTPS sem backend remoto), vai para Mock Store
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
      const response = await fetch(url, {
        ...options,
        headers
      });

      if (response.status === 401) {
        if (!window.location.pathname.endsWith('login.html') && !window.location.pathname.endsWith('1 - Cadastro.html')) {
          this.removeToken();
          window.location.href = 'login.html?expired=true';
          return null;
        }
      }

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
      // Se for falha de rede/conexão recusada ou Mixed Content, faz fallback transparente para o Mock Store
      if (!err.status || err.name === 'TypeError') {
        console.warn(`[Aurora] Backend em ${url} indisponível. Alternando para modo de demonstração local/GitHub Pages.`, err.message);
        return await AuroraMockStore.handle(endpoint, options);
      }
      throw err;
    }
  },

  // ---------------------------------------------------------------------------
  // Métodos de Autenticação
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
  // Catálogo e Filtros de Livros
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

  async getMyRatings() {
    return await this.apiFetch('/collections/ratings/my-ratings');
  },

  // ---------------------------------------------------------------------------
  // Sugestões, Multas e Perfil
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
  // Inicialização da Sidebar e Proteção de Rota
  // ---------------------------------------------------------------------------
  async initSidebar(activePath = '') {
    const isLoginPage = window.location.pathname.endsWith('login.html') || window.location.pathname.endsWith('1 - Cadastro.html');
    if (!this.isAuthenticated()) {
      if (!isLoginPage) {
        window.location.href = 'login.html';
        return;
      }
      return;
    } else if (isLoginPage) {
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

    // Mapeamento das rotas
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

    // Atualiza dados do usuário logado na interface
    let user = this.getUser();
    if (!user) {
      user = await this.getMe();
    }

    if (user) {
      document.querySelectorAll('.user-display-name').forEach(el => el.textContent = user.nome || 'Helena Souza');
      document.querySelectorAll('.user-display-role').forEach(el => {
        const roles = {
          'aluno': 'Leitor Aurora',
          'bibliotecario': 'Bibliotecário',
          'administrador': 'Administrador'
        };
        el.textContent = roles[user.tipo] || user.tipo || 'Leitor Aurora';
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

      const activeRentals = (rentals || []).filter(r => r.status === 'ativo');

      const readBadge = document.querySelector('nav a[data-path="ja-lidos"] span.rounded-full');
      if (readBadge) readBadge.textContent = readBooks.length;

      const wishBadge = document.querySelector('nav a[data-path="lista-de-desejos"] span.rounded-full');
      if (wishBadge) wishBadge.textContent = wishlist.length;

      const rentalBadge = document.querySelector('header a[data-path="sacola-de-alugueis"] span.rounded-full');
      if (rentalBadge) rentalBadge.textContent = activeRentals.length;
    } catch (e) {
      console.warn('Erro ao atualizar badges da sidebar:', e);
    }
  }
};

window.AuroraAPI = AuroraAPI;
