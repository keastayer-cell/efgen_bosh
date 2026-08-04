import { expect, test } from '@playwright/test'

test('shows the Bosh login shell', async ({ page }) => {
  await page.goto('/')

  await expect(page.getByRole('heading', { name: 'Bosh: кузовной ремонт' })).toBeVisible()
  await expect(page.getByRole('button', { name: 'Войти' })).toBeVisible()
})
