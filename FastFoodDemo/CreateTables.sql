drop table FastFoodDemo.users;
drop table FastFoodDemo.food_orders;
drop table FastFoodDemo.food_items;
drop table FastFoodDemo.comments;

create table FastFoodDemo.users (
  id                        bigint not null,
  name                      varchar(255) not null,
  pass                      varchar(511) not null,
  constraint pk_users primary key (id)
);

create table FastFoodDemo.food_orders (
  id                        bigint not null auto_increment,
  food_order_id             bigint not null,
  user_id                   varchar(255) not null,
  food_item                 varchar(255) not null,
  price                     bigint not null,
  quantity                  bigint not null,
  constraint pk_food_orders primary key(id)
);

create table FastFoodDemo.food_items (
  id                        bigint not null auto_increment,
  food_item                 varchar(255) not null,
  price                     bigint not null,
  constraint pk_food_items primary key(id)
);


create table FastFoodDemo.comments (
  id                        bigint not null auto_increment,
  comment_type              varchar(255) not null,
  comment_str               varchar(1024) not null,
  constraint pk_comment primary key(id)
);

insert into FastFoodDemo.users (id, name, pass) values (1, 'jouko', 'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3');
insert into FastFoodDemo.users (id, name, pass) values (2, 'ram', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4');

insert into FastFoodDemo.food_items (id, food_item, price) values (1, 'Cheese Burger', 2);
insert into FastFoodDemo.food_items (id, food_item, price) values (2, 'Double Double', 4);
insert into FastFoodDemo.food_items (id, food_item, price) values (3, 'Fries', 1);
insert into FastFoodDemo.food_items (id, food_item, price) values (4, 'Milk Shake', 390);
