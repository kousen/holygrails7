/*
 * Geb configuration for ContainerGebSpec functional tests.
 *
 * The default container image, selenium/standalone-chrome, is published for
 * amd64 only. On Apple silicon it runs under emulation and Chrome crashes as
 * soon as a test connects. selenium/standalone-firefox is multi-arch, so use
 * Firefox instead. (standalone-chromium is also multi-arch, but Testcontainers
 * rejects it as an unknown substitute for the Chrome image.)
 */
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.remote.RemoteWebDriver

driver = { new RemoteWebDriver(new FirefoxOptions()) }
containerBrowser = 'firefox'
