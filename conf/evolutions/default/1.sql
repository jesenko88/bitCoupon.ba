# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table category (
  id                        bigint not null,
  name                      varchar(255),
  picture                   varchar(255),
  constraint pk_category primary key (id))
;

create table company (
  id                        bigint not null,
  name                      varchar(255),
  email                     varchar(255),
  password                  varchar(255),
  created                   timestamp,
  updated                   timestamp,
  logo                      varchar(255),
  verfied                   boolean,
  constraint pk_company primary key (id))
;

create table coupon (
  id                        bigint not null,
  name                      varchar(255),
  price                     double,
  created_at                timestamp,
  expired_at                timestamp,
  picture                   varchar(255),
  category_id               bigint,
  description               varchar(255),
  remark                    varchar(255),
  constraint pk_coupon primary key (id))
;

create table email_verification (
  id                        varchar(255) not null,
  user_id                   bigint,
  created_on                timestamp,
  is_verified               boolean,
  constraint pk_email_verification primary key (id))
;

create table faq (
  id                        integer not null,
  question                  varchar(255),
  answer                    TEXT,
  constraint pk_faq primary key (id))
;

create table photo (
  id                        integer not null,
  path                      varchar(255),
  save_path                 varchar(255),
  coupon_id                 bigint,
  constraint pk_photo primary key (id))
;

create table transaction_cp (
  id                        bigint not null,
  payment_id                varchar(255),
  money_amount              double,
  token                     varchar(255),
  buyer_id                  bigint,
  seller_id                 bigint,
  coupon_id                 bigint,
  constraint pk_transaction_cp primary key (id))
;

create table user (
  id                        bigint not null,
  username                  varchar(255),
  email                     varchar(255),
  password                  varchar(255),
  is_admin                  boolean,
  created                   timestamp,
  updated                   timestamp,
  profile_picture           varchar(255),
  constraint pk_user primary key (id))
;

create sequence category_seq;

create sequence company_seq;

create sequence coupon_seq;

create sequence email_verification_seq;

create sequence faq_seq;

create sequence photo_seq;

create sequence transaction_cp_seq;

create sequence user_seq;

alter table coupon add constraint fk_coupon_category_1 foreign key (category_id) references category (id) on delete restrict on update restrict;
create index ix_coupon_category_1 on coupon (category_id);
alter table photo add constraint fk_photo_coupon_2 foreign key (coupon_id) references coupon (id) on delete restrict on update restrict;
create index ix_photo_coupon_2 on photo (coupon_id);
alter table transaction_cp add constraint fk_transaction_cp_buyer_3 foreign key (buyer_id) references user (id) on delete restrict on update restrict;
create index ix_transaction_cp_buyer_3 on transaction_cp (buyer_id);
alter table transaction_cp add constraint fk_transaction_cp_seller_4 foreign key (seller_id) references user (id) on delete restrict on update restrict;
create index ix_transaction_cp_seller_4 on transaction_cp (seller_id);
alter table transaction_cp add constraint fk_transaction_cp_coupon_5 foreign key (coupon_id) references coupon (id) on delete restrict on update restrict;
create index ix_transaction_cp_coupon_5 on transaction_cp (coupon_id);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists category;

drop table if exists company;

drop table if exists coupon;

drop table if exists email_verification;

drop table if exists faq;

drop table if exists photo;

drop table if exists transaction_cp;

drop table if exists user;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists category_seq;

drop sequence if exists company_seq;

drop sequence if exists coupon_seq;

drop sequence if exists email_verification_seq;

drop sequence if exists faq_seq;

drop sequence if exists photo_seq;

drop sequence if exists transaction_cp_seq;

drop sequence if exists user_seq;

