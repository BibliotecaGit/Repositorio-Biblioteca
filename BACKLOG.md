# Backlog

## Cadastro

* Área de acesso rápido deve ser retirada

* Deve ser adicionada uma área separada onde administradores e bibliotecários farão login/cadastro

* A parte que tem informações do tipo "3 cópias físicas por livro" deve ser retirada

## Início

Na parte de aluguéis ativos, mesmo quando a conta acaba de ser criada, aparece um livro já alugado e ele aparece como "undefined", além de não aparecer a data de devolução prevista e as extensões já aparecerem esgotadas e não ser possível devolver/cancelar o aluguel

## Prateleira

* Alguns livros aparecem em filtros errados

* A pessoa deveria ser capaz de adicionar mais de um filtro para buscar por um livro

* Não é necessário ter escrito "Catálogo" ao lado de prateleira na sidebar

### Aluguéis ativos

* Como já dito, já aparece um livro como "undefined" mesmo o login sendo novo

* Todos os livros que são alugados aparecem com "undefined"

* Não é possível nem cancelar o aluguel de um livro e nem devolvê-lo, o que consequentemente faz com que a pessoa se torne incapaz de alugar novos livros

## Já lidos

* Na parte de já lidos, uma opção que deveria ser para alugar o livro deveria aparecer onde está escrito "indisponível", isso já é um erro, mas a opção de alugar não deveria existir nessa aba. Sendo assim, no lugar de "indisponível deveria aparecer uma opção de "Avaliar livro", e quando a pessoa clicasse, apareceria uma tela para a pessoa avaliar o livro de 1 - 5 estrelas com direito a fazer um comentário sobre o livro

* às vezes, quando alguém faz um novo cadastro, aparece um livro já adicionado na lista de desejos

## Lista de desejos

* Na parte onde a pessoa deveria ser capaz de alugar o livro, aparece escrito "indisponível"

* às vezes, quando alguém faz um novo cadastro, aparece um livro já adicionado na lista de desejos

## Avaliar livros

* Mesmo quando a conta é nova, aparece um livro já avaliado em 5 estrelas

* Não aparece o nome dos livros avaliados

* A pessoa não consegue retirar sua avaliação

* Quando a pessoa tenta editar a avaliação, ela não consegue mudar a nota da avaliação

* A pessoa deveria ser capaz de adicionar comentários sobre o livro junto com a nota da avaliação (além de poder alterar/editar esse comentário também)

* Deveria haver uma opção onde a pessoa pode ver avaliações e comentários sobre o livro escolhido

## Configurações

### Informações de perfil

#### Nome completo

Gostaria que fosse apenas Nome e segundo nome (sobrenome) ao invés de nome completo

#### URL de foto de perfil

O usuário deve ser capaz de colocar arquivos .png e .jpg ao invés de uma URL

### Endereço

#### CEP

Na parte de CEP, a pessoa deveria ser capaz de colocar apenas números e traços

#### Logradouro/Rua

A pessoa não deveria ser capaz de colocar números nessa parte

#### Número

A pessoa deveria ser capaz de colocar apenas números aqui

### Conta ativa

Quando o nome ou e-mail são grandes demais, os caracteres "transbordam" para fora da área limite

## Sidebar

Quando o usuário acessa algumas abas da sidebar, os ícones da sidebar, a logo da biblioteca aurora e a foto de perfil desaparecem

## Extra 
Quando o usuario tenta traduzir o site para portugues (mesmo que o site ja esteja em portugues), as fontes mudam e parecem ficar totalmete bugadas. 
## Supabase 
Novos usuários não estão sendo registrados no banco de dados que está no supabase. Além de alguns outros problemas ja mencionados (como livros aparecendo ja alugados mesmo o login sendo novo, etc) 
## Perfis exclusivos
Quero dois perfis exclusivos : Para administrador e bibliotecario
### Administrador : 
O administrador poderá ver perfis que possuem livros alugados. O perfil sera exclusivo e ele terá login e senha separados, que ficará localizado em uma aba separada do login e senha do usuário comum.
#### Páginas de administrador
O administrador terá apenas 4 páginas na sua sidebar :
* Inicio (Igual a pagina do usuario comum)
* Estoque de livros : Poderá ver o estoque de livros disponiveis e indisponiveis (separadamente)
* Configurações (Igual a pagina do usuario comum)
* Cadastros : Ele poderá ver login e senha de usuarios com livros alugados (Isso conterá : Quantidade de livros alugados e qual livro foi alugado), bibliotecarios e do próprio administrador, ele tambem tem uma pagina que tem codigos de login, que se um novo usuario quiser criar uma conta como admin ou bibliotecario, precisa dessa senha aleatoria que é atualizada a cada 24 horas, vai ter por dia uma senha especial para criar contas de admin e uma para bibliotecarios (essa opção so tem para admin)
### Bibliotecário : 
O bibliotecário terá a sua parte de login e senha junto com o administrador (ou seja, separado dos usuários comuns) e poderá adicionar/reabastecer livros, colocar no banco de dados e publicar na página. 
#### Páginas do bibliotecário 
* Inicio (Igual a pagina do usuario)
* Estoque de livro (Igual do administrador)
* Configurações (Igual do usuario comum)
* Reestoque : Poderá reabastecer e adicionar livros às prateleiras
## Fila
Quando as três copias de um livro estiverem alugadas, o usuario entrará em uma fila que dara prioridade para quem entrou primeiro na fila
### Pendente
Quando a fila chegar no maximo de 10 pessoas, a pessoa entrará em pendente e não sera possivel alugar o livro
Quando as três copias de um livro estiverem alugadas
## Alguns problemas que aparecem no console 
### CONFIGURAÇÕES, AVALIAR LIVROS, LISTA DE DESEJOS E JA LIDOS
https://lh3.googleusercontent.com/aida/AEtjO1Vl0ovc58Z8FmVMNKvb05Ma0aTILPIO9JOPXsj2IF5CawjY5P-6BRC8pamuGqFbgclo-iP4nFSdWgblkn_iFgtR7hx5sEcXNDFQ9fbV-AwJfBx4pCeFmFPQVTNr3t4DZioVSZRrVP6EFMUp-f0KhQcApiY6tZP5kOR8zziL8a2vgSRU63YEX8g5BImR1JA8aEfj3fYjlk7XEfKL4Bq4XmUQx0I5I8RhoN87iEfOGuAivbWkNSYommjR_9YtExwHeZv_id0WCc67WRs

Request method

GET

Status code

403 Forbidden

Referrer policy

strict-origin-when-cross-origin
### PRATELEIRA
https://images.unsplash.com/photo-1532012164546-f432f2e3777f?w=400&q=80

Request method

GET

Status code

404 Not Found

Referrer policy

strict-origin-when-cross-origin
