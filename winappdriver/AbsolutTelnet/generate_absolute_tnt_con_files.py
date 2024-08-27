import unittest
import configparser
from appium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.common.action_chains import ActionChains
from selenium.webdriver.common.keys import Keys

class AbsolutTelnetSSHClientTest(unittest.TestCase):

    def __init__(self, methodName='runTest', hostname=None, username=None, port=None, debug=None, repeat_count=1):
        super(AbsolutTelnetSSHClientTest, self).__init__(methodName)
        # In this case we want to use class attributes, as for setUpClass() we need to be able to refer to them
        #Todo unfortunately this destroys the initial idea of parallelizing, but because of client command in ECDSA-Sampler there might not be a need for parallelization
        self.hostname = hostname
        self.username = username
        self.port = port
        self.debug = debug
        self.repeat_count = repeat_count


    def setUp(self):
        executable_path = r'C:\Program Files\AbsoluteTelnet\Program\AbsoluteTelnet.exe'
        desired_caps = {
            'platformName': 'Windows',
            'deviceName': 'WindowsPC',
            'app': executable_path
        }
        self.driver = webdriver.Remote(
            command_executor='http://127.0.0.1:4723',
            desired_capabilities=desired_caps
        )

    def test_build_profiles(self):
        def debug_print(message):
                if self.debug:
                        print(message)


        elem_to_find = WebDriverWait(self.driver, 5).until(
            EC.visibility_of_element_located(
                (By.NAME,'Öffnen')
            )
        )
        elem_to_find.click()
        ActionChains(self.driver).key_down(Keys.ALT).send_keys(Keys.F4).key_up(Keys.ALT).perform()

        init_con_button = WebDriverWait(self.driver, 30).until(
            EC.presence_of_element_located((By.NAME, 'Verbinden'))
        )
        self.assertIsNotNone(init_con_button)
        init_con_button.click()

        xpath_for_hostname= '//Edit[@ClassName="RichEdit20W" and @Name="Hostname"]'

        # Wait until the Edit field is visible
        edit_field_hostname = WebDriverWait(self.driver, 10).until(
            EC.visibility_of_element_located((By.XPATH, xpath_for_hostname))
        )

        # Clear any existing text (if needed) and send the new text
        edit_field_hostname.clear()  # Optional: Use if you want to clear the existing text
        edit_field_hostname.send_keys(self.hostname)  #

        xpath_for_port= '//Edit[@ClassName="RichEdit20W" and @Name="Port"]'

        # Wait until the Edit field is visible
        edit_field_port = WebDriverWait(self.driver, 10).until(
            EC.visibility_of_element_located((By.XPATH, xpath_for_port))
        )

        # Clear any existing text (if needed) and send the new text
        edit_field_port.clear()  # Optional: Use if you want to clear the existing text
        edit_field_port.send_keys(self.port)  #

        xpath_for_radiobutton = '//RadioButton[@ClassName="Button" and @Name="Username:"]'
        # Wait until the RadioButton is visible
        radio_button = WebDriverWait(self.driver, 10).until(
            EC.visibility_of_element_located((By.XPATH, xpath_for_radiobutton))
        )
        # Click the RadioButton
        radio_button.click()

        xpath_username = "//Tab[@ClassName='SysTabControl32'][@Name='Tab1']//Edit[@ClassName='RichEdit20W' and @Name='']"


        edit_field_username = WebDriverWait(self.driver, 10).until(
            EC.visibility_of_element_located((By.XPATH, xpath_username))
        )

        # Clear any existing text (if needed) and send the new text
        edit_field_username.clear()  # Optional: Use if you want to clear the existing text
        edit_field_username.send_keys(self.username)

        xpath_for_checkbox = '//CheckBox[@ClassName="Button" and @Name="RSA/DSA Schlüssel zum Anmelden"]'

        # Wait until the Checkbox is visible
        checkbox = WebDriverWait(self.driver, 10).until(
            EC.visibility_of_element_located((By.XPATH, xpath_for_checkbox))
        )


        # Check if the checkbox is already selected, and if not, click to select it
        if not checkbox.is_selected():
            checkbox.click()

        xpath_for_edit_element = '//Edit[@ClassName="RichEdit20W" and @Name="Key File:"]'

        # Wait until the Edit element is visible
        edit_element = WebDriverWait(self.driver, 10).until(
            EC.visibility_of_element_located((By.XPATH, xpath_for_edit_element))
        )

        # Example action: Clear any existing text and send new text
        edit_element.clear()  # Clear the field (optional)
        config = configparser.ConfigParser()
        config.read('config.ini')
        #backslash is converted into # bcs of german keyboard layout usage, thus use this workaround
        backslash = Keys.ALT + Keys.NUMPAD9 + Keys.NUMPAD2 + Keys.ALT
        modified_path = config['DEFAULT']['privatekey_path'].replace('\\', backslash)
        edit_element.send_keys(modified_path)
        entered_text = edit_element.get_attribute("Value.Value")  # Attribute might vary depending on the application
        print("Using keypath from config.ini:  " + entered_text)
        xpath_for_button = "//Button[@ClassName='Button'][@Name='OK']"
        ok_button = WebDriverWait(self.driver, 10).until(
            EC.visibility_of_element_located((By.XPATH, xpath_for_button))
        )
        ok_button.click()
        #Connect to specified host connection
        init_con_button.click()

        save_button = WebDriverWait(self.driver, 30).until(
            EC.presence_of_element_located((By.NAME, 'Speichern'))
        )
        self.assertIsNotNone(save_button)
        save_button.click()

        xpath_for_filename = "//*[@ClassName='Edit'][@Name='Dateiname:']"

        # Wait until the Edit field is visible
        edit_field_filename = WebDriverWait(self.driver, 10).until(
            EC.visibility_of_element_located((By.XPATH, xpath_for_filename))
        )

        # Clear any existing text (if needed) and send the new text
        edit_field_filename.clear()  # Optional: Use if you want to clear the existing text
        edit_field_filename.send_keys(self.username+ "@"+self.hostname+"/"+ str(self.port))
        save_button = WebDriverWait(self.driver, 30).until(
            EC.presence_of_element_located((By.NAME, 'Speichern'))
        )
        self.assertIsNotNone(save_button)
        save_button.click()
        close_button = WebDriverWait(self.driver, 30).until(
            EC.presence_of_element_located((By.NAME, 'Schließen'))
        )
        self.assertIsNotNone(close_button)
        close_button.click()







