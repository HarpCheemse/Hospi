<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
  <head>
    <title>Hospi | Hotel Booking</title>
    <%@include file="/WEB-INF/views/layout/header.jsp"%>
  </head>

  <body class="bg-background text-text">
    <header class="fixed top-0 left-0 z-50 w-full">
      <div class="mx-auto max-w-7xl px-6 py-5">
        <div
          class="flex items-center justify-between rounded-2xl bg-surface/95 px-5 py-4 shadow-card"
        >
          <a href="${pageContext.request.contextPath}/" class="flex items-center gap-3">
            <img
              class="h-10 w-10 rounded-xl"
              src="${pageContext.request.contextPath}/images/logo.png"
              alt="Hospi logo"
            />
            <span class="text-lg font-bold">Hospi</span>
          </a>

          <nav class="hidden items-center gap-8 text-sm font-medium text-muted md:flex">
            <a class="text-text" href="${pageContext.request.contextPath}/">Home</a>
            <a href="#rooms">Rooms</a>
            <a href="#services">Services</a>
            <a href="#about">About</a>
            <a href="#contact">Contact</a>
          </nav>

          <a
            class="rounded-full bg-primary px-5 py-3 text-sm font-semibold text-text shadow-card"
            href="#booking"
          >
            Book Now
          </a>
        </div>
      </div>
    </header>

    <main>
      <section class="relative min-h-screen overflow-hidden">
        <img
          class="absolute inset-0 h-full w-full object-cover"
          src="${pageContext.request.contextPath}/images/hotel1.jpg"
          alt="Luxury hotel lobby"
        />
        <div class="absolute inset-0 bg-text/65"></div>

        <div class="relative mx-auto flex min-h-screen max-w-7xl flex-col justify-end px-6 pb-10 pt-32">
          <div class="max-w-3xl pb-12 text-white">
            <p class="mb-4 text-sm font-semibold uppercase tracking-wide text-primary">
              Calm stays, easy booking
            </p>
            <h1 class="text-5xl font-bold leading-tight md:text-7xl">
              Find your next comfortable stay with Hospi
            </h1>
            <p class="mt-6 max-w-2xl text-lg text-white/80">
              Explore selected rooms, manage reservations, and enjoy a smooth hotel experience from arrival to checkout.
            </p>
          </div>

          <form
            id="booking"
            class="grid gap-4 rounded-2xl bg-surface p-5 shadow-soft md:grid-cols-5"
          >
            <label class="block">
              <span class="mb-2 block text-xs font-semibold uppercase text-subtle">Location</span>
              <select class="w-full rounded-xl border border-border bg-background px-4 py-3 text-sm">
                <option>Ho Chi Minh City</option>
                <option>Da Nang</option>
                <option>Ha Noi</option>
              </select>
            </label>

            <label class="block">
              <span class="mb-2 block text-xs font-semibold uppercase text-subtle">Check In</span>
              <input
                class="w-full rounded-xl border border-border bg-background px-4 py-3 text-sm"
                type="date"
              />
            </label>

            <label class="block">
              <span class="mb-2 block text-xs font-semibold uppercase text-subtle">Check Out</span>
              <input
                class="w-full rounded-xl border border-border bg-background px-4 py-3 text-sm"
                type="date"
              />
            </label>

            <label class="block">
              <span class="mb-2 block text-xs font-semibold uppercase text-subtle">Guests</span>
              <select class="w-full rounded-xl border border-border bg-background px-4 py-3 text-sm">
                <option>2 guests</option>
                <option>1 guest</option>
                <option>3 guests</option>
                <option>4 guests</option>
              </select>
            </label>

            <button
              class="mt-6 rounded-xl bg-primary px-5 py-3 font-semibold text-text md:mt-7"
              type="button"
            >
              Search Rooms
            </button>
          </form>
        </div>
      </section>

      <section id="about" class="mx-auto grid max-w-7xl gap-12 px-6 py-24 lg:grid-cols-2">
        <div>
          <p class="mb-3 text-sm font-semibold uppercase text-accent">About Hospi</p>
          <h2 class="text-4xl font-bold leading-tight">
            A modern hotel booking experience for guests and staff
          </h2>
        </div>
        <div class="space-y-6 text-muted">
          <p>
            Hospi helps guests discover rooms quickly while giving hotel staff a clear operational dashboard for bookings, arrivals, payments, and room readiness.
          </p>
          <div class="grid grid-cols-3 gap-4">
            <div class="rounded-2xl bg-surface p-5 shadow-card">
              <p class="text-3xl font-bold text-text">120+</p>
              <p class="mt-1 text-sm">Rooms</p>
            </div>
            <div class="rounded-2xl bg-surface p-5 shadow-card">
              <p class="text-3xl font-bold text-text">24/7</p>
              <p class="mt-1 text-sm">Service</p>
            </div>
            <div class="rounded-2xl bg-surface p-5 shadow-card">
              <p class="text-3xl font-bold text-text">4.8</p>
              <p class="mt-1 text-sm">Rating</p>
            </div>
          </div>
        </div>
      </section>

      <section id="rooms" class="bg-surface-muted py-24">
        <div class="mx-auto max-w-7xl px-6">
          <div class="mb-10 flex flex-col justify-between gap-4 md:flex-row md:items-end">
            <div>
              <p class="mb-3 text-sm font-semibold uppercase text-accent">Featured Rooms</p>
              <h2 class="text-4xl font-bold">Choose a room that fits your trip</h2>
            </div>
            <a class="font-semibold text-accent" href="#booking">View all rooms</a>
          </div>

          <div class="grid gap-6 md:grid-cols-3">
            <article class="overflow-hidden rounded-2xl bg-surface shadow-card">
              <img
                class="h-64 w-full object-cover"
                src="${pageContext.request.contextPath}/images/hotel2.jpg"
                alt="Deluxe room"
              />
              <div class="p-6">
                <div class="mb-3 flex items-center justify-between">
                  <h3 class="text-xl font-bold">Deluxe Room</h3>
                  <span class="rounded-full bg-primary-soft px-3 py-1 text-xs font-semibold">$89/night</span>
                </div>
                <p class="text-sm text-muted">Bright room with king bed, work desk, city view, and daily housekeeping.</p>
              </div>
            </article>

            <article class="overflow-hidden rounded-2xl bg-surface shadow-card">
              <img
                class="h-64 w-full object-cover"
                src="${pageContext.request.contextPath}/images/hotel3.jpg"
                alt="Pool suite"
              />
              <div class="p-6">
                <div class="mb-3 flex items-center justify-between">
                  <h3 class="text-xl font-bold">Pool Suite</h3>
                  <span class="rounded-full bg-primary-soft px-3 py-1 text-xs font-semibold">$149/night</span>
                </div>
                <p class="text-sm text-muted">Spacious suite close to leisure facilities, ideal for couples and short breaks.</p>
              </div>
            </article>

            <article class="overflow-hidden rounded-2xl bg-surface shadow-card">
              <img
                class="h-64 w-full object-cover"
                src="${pageContext.request.contextPath}/images/hotel1.jpg"
                alt="Executive stay"
              />
              <div class="p-6">
                <div class="mb-3 flex items-center justify-between">
                  <h3 class="text-xl font-bold">Executive Stay</h3>
                  <span class="rounded-full bg-primary-soft px-3 py-1 text-xs font-semibold">$199/night</span>
                </div>
                <p class="text-sm text-muted">Premium comfort with lounge access, flexible check-in, and concierge support.</p>
              </div>
            </article>
          </div>
        </div>
      </section>

      <section id="services" class="mx-auto max-w-7xl px-6 py-24">
        <div class="grid gap-10 lg:grid-cols-3">
          <div>
            <p class="mb-3 text-sm font-semibold uppercase text-accent">Services</p>
            <h2 class="text-4xl font-bold leading-tight">Everything guests expect from a modern hotel</h2>
          </div>

          <div class="grid gap-4 md:grid-cols-2 lg:col-span-2">
            <div class="rounded-2xl bg-surface p-6 shadow-card">
              <h3 class="mb-2 text-lg font-bold">Fast Check-In</h3>
              <p class="text-sm text-muted">Reception workflows keep arrivals organized and reduce waiting time.</p>
            </div>
            <div class="rounded-2xl bg-surface p-6 shadow-card">
              <h3 class="mb-2 text-lg font-bold">Secure Payments</h3>
              <p class="text-sm text-muted">Payment status and booking confirmation stay visible to the front desk.</p>
            </div>
            <div class="rounded-2xl bg-surface p-6 shadow-card">
              <h3 class="mb-2 text-lg font-bold">Room Management</h3>
              <p class="text-sm text-muted">Track clean, occupied, reserved, and out-of-service rooms in one place.</p>
            </div>
            <div class="rounded-2xl bg-surface p-6 shadow-card">
              <h3 class="mb-2 text-lg font-bold">Guest Support</h3>
              <p class="text-sm text-muted">Handle requests, late checkout, and service calls without losing context.</p>
            </div>
          </div>
        </div>
      </section>

      <section class="mx-auto max-w-7xl px-6 pb-24">
        <div class="relative overflow-hidden rounded-2xl bg-text p-8 text-white md:p-12">
          <img
            class="absolute inset-0 h-full w-full object-cover opacity-25"
            src="${pageContext.request.contextPath}/images/hotel3.jpg"
            alt="Hotel pool"
          />
          <div class="relative max-w-2xl">
            <p class="mb-3 text-sm font-semibold uppercase text-primary">Ready for your stay?</p>
            <h2 class="text-4xl font-bold">Reserve a room and let the hotel team handle the rest.</h2>
            <a
              class="mt-8 inline-block rounded-full bg-primary px-6 py-3 font-semibold text-text"
              href="#booking"
            >
              Start Booking
            </a>
          </div>
        </div>
      </section>
    </main>

    <footer id="contact" class="border-t border-border bg-surface">
      <div class="mx-auto flex max-w-7xl flex-col gap-6 px-6 py-10 md:flex-row md:items-center md:justify-between">
        <div>
          <p class="text-lg font-bold">Hospi</p>
          <p class="mt-1 text-sm text-muted">Hotel booking and management for student project delivery.</p>
        </div>
        <div class="flex gap-6 text-sm text-muted">
          <a href="#rooms">Rooms</a>
          <a href="#services">Services</a>
          <a href="#booking">Booking</a>
        </div>
      </div>
    </footer>
  </body>
</html>
