"""!/usr/bin/python2.7----------------------------------------------------------
Class that holds the Componets for the main view screen of PQMFG data aquistion system

Written by: Max Seifert AKA cytznx
-------------------------------------------------------------------------------"""

#Gui Elements
import wx
from wxPython.wx import *

#cool stuff
import os, sys
import user

class mainScreenInfoPanel(wx.Panel):
	def __init__(self, parent, frame, hideMouse, size):

		# initialize Pannel
		wx.Panel.__init__(self, parent, size=size)

		#Hides the currser
		if hideMouse:
			self.SetCursor(wx.StockCursor(wx.CURSOR_BLANK))

		#set Background color
		self.SetBackgroundColour("black")

		self.LocalBorder = 5

		# I save this ... for setting size later but i dont think i need to use it...
		self.myParent = parent

		#The Machine Number
		self.MachineNumberHeader = wx.StaticText(self, -1, "##",
			pos =(1*self.LocalBorder,self.LocalBorder))

		#Basic Formating
		self.MachineNumberHeader.SetFont(wx.Font(40, wx.SWISS, wx.NORMAL, wx.BOLD))
		self.MachineNumberHeader.SetSize(self.MachineNumberHeader.GetBestSize())
		self.MachineNumberHeader.SetForegroundColour((0,255,0)) # set text color
		self.MachineNumberHeader.SetBackgroundColour((0,0,255)) # set text back color


		#Creates the "PQMFG Data Aquision System________________ " Header
		mainHeader = wx.StaticText(self, -1, "PQMFG Data Aquision System________________ ",
			pos =((2*self.LocalBorder) + 91, 3.5*self.LocalBorder))

		#Basic Formating
		mainHeader.SetFont(wx.Font(24, wx.SWISS, wx.NORMAL, wx.BOLD))
		mainHeader.SetSize(mainHeader.GetBestSize())
		mainHeader.SetForegroundColour((128,255,255)) # set text color
		#mainHeader.SetBackgroundColour((0,0,255)) # set text back color

		_FirstComnOffset = 5*self.LocalBorder
		_SecondOffset = 366
		_ThirdOffset = 700-30

		self._CountDspDic, returnLoc = self.CreateDspColumn(
			startingKeys = [("Total Peaces","######"), ("Peaces Boxed","######"), ("Peaces Scraped","######")],
			startingLoc = (_FirstComnOffset, self.MachineNumberHeader.GetBestSize()[1]+2*self.LocalBorder))

		self._PPMDspDic, returnLoc = self.CreateDspColumn(
			startingKeys = [("Hourly(Peaces/Minute)","###.##"), ("Total(Peaces/Minute)","###.##")],
			startingLoc = (_FirstComnOffset, returnLoc[1]+2*self.LocalBorder))

		self._LineStatusDspDic, returnLoc = self.CreateDspColumn(
			startingKeys = [("Current WO","N/A"), ("WO Runtime","00:00:00"), ("LineStatus","Inactice")],
			startingLoc = (_SecondOffset,self.MachineNumberHeader.GetBestSize()[1]+2*self.LocalBorder),
			SecondColColor=(255, 0, 0))
		self._MnDwnTimesDspDic, returnLoc = self.CreateDspColumn(
			startingKeys = [("Maintence","00:00:00"), ("Inventory","00:00:00"), ("Q/A and Q/C","00:00:00")],
			startingLoc = (_ThirdOffset,self.MachineNumberHeader.GetBestSize()[1]+2*self.LocalBorder),
			SecondColColor=(255, 0, 0))

		self._subDwnTimesDspDic, returnLoc = self.CreateDspColumn(
			startingKeys = [("Total","00:00:00"), ("Change Over","00:00:00")],
			startingLoc = (_ThirdOffset-7,returnLoc[1]+(2*self.LocalBorder)),
			SecondColColor=(255, 0, 0))

		#Creates the "PQMFG Data Aquision System________________ " Header
		subHeader = wx.StaticText(self, -1, "______Current Employees____________________________ ",
			pos =((2*self.LocalBorder) , returnLoc[1]+2*self.LocalBorder))

		#Basic Formating
		subHeader.SetFont(wx.Font(24, wx.SWISS, wx.NORMAL, wx.BOLD))
		subHeader.SetSize(subHeader.GetBestSize())
		subHeader.SetForegroundColour((0,128,255)) # set text color
		#mainHeader.SetBackgroundColour((0,0,255)) # set text back color

		self._HeaderBottom = returnLoc[1]+2*self.LocalBorder + subHeader.GetBestSize()[1]

	def CreateDspColumn(self, startingKeys, startingLoc, Size=12, Spacers=(10,5), FirstColColor=(255, 255, 255), SecondColColor=(0, 255, 0)):
		curHeaderPos = startingLoc
		curHeaderSpacer = []

		#Dictionary Retured for updating pannels latter
		curDspDct = dict()

		#Create Static Text For keepping peace count
		for header,_ in startingKeys:

			curDspDct[header] = None
			subHeader = wx.StaticText(self, -1, header+": ",
				pos =curHeaderPos)

			subHeader.SetFont(wx.Font(Size, wx.SWISS, wx.NORMAL, wx.BOLD))
			subHeader.SetSize(subHeader.GetBestSize())
			subHeader.SetForegroundColour(FirstColColor) # set text color

			curHeaderPos = (curHeaderPos[0],curHeaderPos[1]+subHeader.GetBestSize()[1]+Spacers[1])

			curHeaderSpacer.append(subHeader.GetBestSize()[0])


		curHeaderPos = (curHeaderPos[0]+max(curHeaderSpacer)+Spacers[0], startingLoc [1])
		curHeaderSpacer = []

		for key, spacer in startingKeys:

			curDspDct[key] = wx.StaticText(self, -1, spacer,
				pos =curHeaderPos)

			curDspDct[key].SetFont(wx.Font(Size, wx.SWISS, wx.NORMAL, wx.BOLD))
			curDspDct[key].SetSize(curDspDct[key].GetBestSize())
			curDspDct[key].SetForegroundColour(SecondColColor) # set text color
			curHeaderSpacer.append(curDspDct[key].GetBestSize()[0])

			curHeaderPos = (curHeaderPos[0], curHeaderPos[1]+curDspDct[key].GetBestSize()[1]+Spacers[1])

		return curDspDct , (curHeaderPos[0]+max(curHeaderSpacer), curHeaderPos[1])

	def RefreshData(self, HeaderData, EmployeeList):

		for Header, Data, Color in HeaderData:
			_curOperator = None

			if Header == "Machine Number":
				_curOperator = self.MachineNumberHeader

			elif Header in self._CountDspDic.keys():
				_curOperator = self._CountDspDic[Header]

			elif Header in self._PPMDspDic.keys():
				_curOperator = self._PPMDspDic[Header]

			elif Header in self._LineStatusDspDic.keys():
				_curOperator = self._LineStatusDspDic[Header]

			elif Header in self._MnDwnTimesDspDic.keys():
				_curOperator = self._MnDwnTimesDspDic[Header]

			elif Header in self._subDwnTimesDspDic.keys():
				_curOperator = self._subDwnTimesDspDic[Header]

			if _curOperator is not None:
				_curOperator.SetLabel(Data)
				if Color is not None:
					_curOperator.SetForegroundColour(Color) # set text color


	def OnPaint(self, evt=None):
		"""set up the device context (DC) for painting"""
		dc = wx.PaintDC(self)
		dc.BeginDrawing()
		dc.SetPen(wx.Pen("red",style=wx.SOLID))
		dc.SetBrush(wx.Brush("red", wx.SOLID))
		# set x, y, w, h for rectangle
		dc.DrawRectangle(self.LocalBorder,self.LocalBorder,200, 200)
		dc.EndDrawing()

class mainScreenButtonPanel(wx.Panel):

		def __init__(self, parent, frame, hideMouse, size,):

			#Tagging Parent
			self.parent = parent
			self.curFrame = frame
			self.gap = 5

			#some universal variables
			self.button_width = (size[0]-(3*self.gap))/2
			self.button_height = ((int(((2.0/3.0)*(size[1]))-(8*self.gap)))/7)
			self.dialog_width = (size[0])-(3*self.gap)
			self.dialog_height = ((1.0/3.0)*(size[1])) - (3*self.gap)

			self.gap = self.gap

			# Create the Button/Message Panel
			wx.Panel.__init__(self, parent, size=size)

			#Hides the currser
			if hideMouse:
				self.SetCursor(wx.StockCursor(wx.CURSOR_BLANK))

			# set Background color
			self.SetBackgroundColour("black")

			# 1 Create Button Loading in New Work Order
			LoadNewWOButton = wx.Button(self, label="Load New WO",
				pos=(self.gap, self.gap), size=(self.button_width, self.button_height))

			LoadNewWOButton.Bind(wx.EVT_BUTTON, self.LoadNewWOButtonEvent, )

			# 2 Create Button for Deleting in New Work Order
			DeletWOButton = wx.Button(self, label="Delete Current WO",
				pos=(self.gap, 2*self.gap+1*self.button_height),
				size=(self.button_width, self.button_height))

			DeletWOButton.Bind(wx.EVT_BUTTON, self.DeletWOButtonEvent, )

			# 3 Create Button for adding Employee
			AddEmployeeButton = wx.Button(self, label="Add Employee",
				pos=(self.gap, 3*self.gap+2*self.button_height),
				size=(self.button_width, self.button_height))

			AddEmployeeButton.Bind(wx.EVT_BUTTON, self.AddEmployeeButtonEvent, )

			# 4 Create Button for Bring Line out of down time
			LineUpButton = wx.Button(self, label="Bring Line Up",
				pos=(self.gap, 4*self.gap+3*self.button_height),
				size=(self.button_width, self.button_height))

			LineUpButton.Bind(wx.EVT_BUTTON, self.LineUpButtonEvent, )

			#
			########################SECOND COLUM##################################
			#

			# 5 Create Button for Completing the current WO
			CompleteCurrentWOButton = wx.Button(self, label="Complete Current WO",
				pos=(2*self.gap + self.button_width, self.gap),
				size=(self.button_width, self.button_height))

			CompleteCurrentWOButton.Bind(wx.EVT_BUTTON, self.CompleteCurrentWOButtonEvent, )

			# 6 Create Button for adding Adjusting the Current WO Count
			AdjustCountButton = wx.Button(self, label="Adjust Current Count",
				pos=(2*self.gap + self.button_width, 2*self.gap+1*self.button_height),
				size=(self.button_width, self.button_height))

			AdjustCountButton.Bind(wx.EVT_BUTTON, self.AdjustCountButtonEvent, )

			# 7 Create Button for REmoving the current Employee
			removeEmployeeButton = wx.Button(self, label="Remove Employee",
				pos=(2*self.gap + self.button_width, 3*self.gap+2*self.button_height),
				size=(self.button_width, self.button_height))

			removeEmployeeButton.Bind(wx.EVT_BUTTON, self.removeEmployeeButtonEvent, )

			# 8 Create Button for Bringing Line Down
			LineDownButton = wx.Button(self, label="Bring Line Down",
				pos=(2*self.gap + self.button_width, 4*self.gap+3*self.button_height),
				size=(self.button_width, self.button_height))

			LineDownButton.Bind(wx.EVT_BUTTON, self.SmoothMoreButtonEvent, )


			#
			########################Bottom Row##################################
			#

			# Create Button for switching view to fill sheet
			FillSheetButton = wx.Button(self, label="Fill Sheet",
				pos=(self.gap, 5*self.gap+4*self.button_height),
				size=((self.button_width*2)+self.gap, self.button_height))

			FillSheetButton.Bind(wx.EVT_BUTTON, self.FillSheetButtonEvent, )

			# 12 Create Button for seting Email Updates
			SetEmailButton = wx.Button(self, label="Set Email Updates",
				pos=(self.gap, 6*self.gap+5*self.button_height),
				size=((self.button_width*2)+self.gap, self.button_height))

			SetEmailButton.Bind(wx.EVT_BUTTON, self.SetEmailButtonEvent, )

			# 13Create Button for seting the printer
			SetPrinterButton = wx.Button(self, label="Set Printer",
				pos=(self.gap, (7*self.gap)+(6*self.button_height)),
				size=((self.button_width*2)+self.gap, self.button_height))

			SetPrinterButton.Bind(wx.EVT_BUTTON, self.SetPrinterButtonEvent, )

			#
			########################Readout Pannel ##################################
			#
			self._messagePannel = wx.TextCtrl( self, -1,
				pos=(self.gap, (8*self.gap)+(7*self.button_height)),
				size = (self.dialog_width, self.dialog_height),
				style = wx.TE_MULTILINE | wx.TE_READONLY)

		def LoadNewWOButtonEvent(self, event=None):
			pass

		def DeletWOButtonEvent(self, event=None):
			pass

		def AddEmployeeButtonEvent(self, event=None):
			pass

		def LineUpButtonEvent(self, event=None):
			pass

		def CompleteCurrentWOButtonEvent(self, event=None):
			pass

		def AdjustCountButtonEvent(self, event=None):
			pass

		def removeEmployeeButtonEvent(self, event=None):
			pass

		def SmoothMoreButtonEvent(self, event=None):
			pass

		def FillSheetButtonEvent(self, event=None):
			self.curFrame.TogglFillSheet(event)

		def SetPrinterButtonEvent(self, event=None):
			pass

		def SetEmailButtonEvent(self, event=None):
			pass

		def UndoFilter(self):
			pass


		def WriteToTextPannel(self, MessageString):
			if(not(MessageString == None)):
				self._messagePannel.AppendText(MessageString)
