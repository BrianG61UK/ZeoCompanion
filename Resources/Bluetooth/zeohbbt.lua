-- Zeo headband bluetooth (Zeo HB BT) protocol
-- Dated 5 Mar 2016
-- worker functions
function zeohbbt_identify(buffer)
  if buffer:len() < 8 or buffer(0,4):string() ~= "HMSG" then 
	return -1,""
  end

  local msgType = buffer(7,1):uint()
  local msgTypeStr = ""
  local msgTypeLen = 12
  if msgType == 0 then 
    msgTypeStr = "NULL"
    msgTypeLen = 12
  elseif msgType == 1 then 
    msgTypeStr = "COMMAND_REQUEST"
    msgTypeLen = 16
  elseif msgType == 2 then 
    msgTypeStr = "HB_ACKNOWLEDGE"
    msgTypeLen = 16
  elseif msgType == 3 then 
    msgTypeStr = "HB_STARTING"
    msgTypeLen = 116
  elseif msgType == 4 then 
    msgTypeStr = "HB_STATE_CHANGE_REPORT"
    msgTypeLen = 16
  elseif msgType == 5 then 
    msgTypeStr = "REPORT_ACCELEROMETER"
    msgTypeLen = 84
  elseif msgType == 6 then 
    msgTypeStr = "REPORT_ERROR"
    msgTypeLen = 16
  elseif msgType == 7 then 
    msgTypeStr = "SLEEP_REPORT"
    msgTypeLen = 1156
  elseif msgType == 8 then 
    msgTypeStr = "SET_UP_BLUETOOTH"
    msgTypeLen = 68
  elseif msgType == 9 then 
    msgTypeStr = "STATE_REPORT"
    msgTypeLen = 28
  elseif msgType == 10 then 
    msgTypeStr = "TIME_QUERY"
    msgTypeLen = 20
  elseif msgType == 11 then 
    msgTypeStr = "TIME_REPORT"
    msgTypeLen = 24
  elseif msgType == 12 then 
    msgTypeStr = "WAKEUP_NOTIFY"
    msgTypeLen = 16
  elseif msgType == 13 then 
    msgTypeStr = "WAKEUP_WINDOW"
    msgTypeLen = 24
  end
  return msgTypeLen, msgTypeStr
end

function zeohbbt_part1Decode(buffer,pinfo,tree,msgTypeLen,msgTypeStr)
  local msgType = buffer(7,1):uint()
  local infoStr = "Zeo HB BT Msg type=" .. msgType .. " " .. msgTypeStr .. " PARTIAL-PACKET"
  pinfo.cols.protocol = "Zeo HB BT"
  pinfo.cols.info = infoStr
  local subtree = tree:add(zeohbbt_proto,buffer(),infoStr)
  subtree:add(buffer(0,4),"Header: " .. buffer(0,4):string())
  subtree:add(buffer(6,1),"Protocol ver: " .. buffer(6,1):uint())
  subtree:add(buffer(7,1),"Message type: " .. msgType .. " " .. msgTypeStr)
  if buffer:len() >= 10 then
    subtree:add(buffer(9,1),"Seq#: " .. buffer(9,1):uint())
  end
  if buffer:len() >= 11 then
    local workStr = "Data content len: " .. buffer(10,2):le_uint()
    if buffer:len() < 13 then
      workStr = workStr .. " [Data content follows in subsequent packets]"
    end
    subtree:add(buffer(10,2),workStr)
  end
end

function zeohbbt_part2Decode(buffer,pinfo,tree,msgType,msgTypeLen,msgTypeStr)
  local infoStr = "Zeo HB BT Msg type=" .. msgType .. " " .. msgTypeStr .. " PARTIAL-PACKET-CONTINUED"
  pinfo.cols.protocol = "Zeo HB BT"
  pinfo.cols.info = infoStr
  local subtree = tree:add(zeohbbt_proto,buffer(),infoStr)
  subtree:add(buffer(0,buffer:len()),"[Incomplete Data content]")
end

function zeohbbt_fullDecode(buffer,pinfo,tree,msgTypeLen,msgTypeStr)
  local msgType = buffer(7,1):uint()
  local infoMsg = ""
  local infoAndTreeMsg = ""
  if buffer:len() >= 13 then
    if msgType == 2 then
	  infoAndTreeMsg = "Previous Sequence No: " .. buffer(12,1):uint()
    elseif msgType == 4 then 
	  infoAndTreeMsg = "Event: " .. buffer(12,1):uint()
    elseif msgType == 6 then 
	  iinfoAndTreeMsg = "Error: " .. buffer(12,1):uint()
    elseif msgType == 12 then 
	  infoAndTreeMsg = "Wake reason: " .. buffer(12,1):uint()
    elseif msgType == 1 then 
	  infoAndTreeMsg = "Command: " .. buffer(12,1):uint()
	elseif msgType == 10 and buffer:len() >= 20 then
	   infoMsg = "Current_time: " .. buffer(12,4):le_uint() .. "." .. buffer(16,4):le_uint()
	elseif msgType == 11 and buffer:len() >= 24 then
	   infoMsg = "Current_time: " .. buffer(12,4):le_uint() .. "." .. buffer(16,4):le_uint()
	elseif msgType == 3 and buffer:len() >= 116 then
	  infoMsg = "Device_name=" .. buffer(12,20):stringz()
	  infoMsg = infoMsg .. "; Model=" .. buffer(48,12):stringz()
	  infoMsg = infoMsg .. "; Serial=" .. buffer(60,16):stringz()
	  infoMsg = infoMsg .. "; HWver=" .. buffer(44,4):le_uint()
	  infoMsg = infoMsg .. "; SWver=" .. buffer(76,4):le_uint()
	elseif msgType == 9 and buffer:len() >= 28 then
	  infoMsg = "Status: "
	  if buffer(15,1):uint() > 0 then
	    infoMsg = infoMsg .. "+"
	  else
	    infoMsg = infoMsg .. "-"
	  end
	  infoMsg = infoMsg .. "Docked, "
	  if buffer(16,1):uint() > 0 then
	    infoMsg = infoMsg .. "+"
	  else
	    infoMsg = infoMsg .. "-"
	  end
	  infoMsg = infoMsg .. "OnHead, "
	  infoMsg = infoMsg .. "BattState=" .. buffer(21,1):uint()
	  infoMsg = infoMsg .. ", Last algMode=" .. buffer(23,1):uint()
	 elseif msgType == 7 and buffer:len() >= 1156 then
	   infoMsg = "Start=" .. buffer(12,4):le_uint()
	   if buffer(184,4):le_uint() > 0 then
	     local delta1 = buffer(184,4):le_uint() - buffer(12,4):le_uint()
	     infoMsg = infoMsg .. ",Night=+" .. delta1
	   end
	   if buffer(60,4):le_uint() > 0 then
	     local delta2 = buffer(60,4):le_uint() - buffer(12,4):le_uint()
	     infoMsg = infoMsg .. ",End=+" .. delta2
	   end
	   infoMsg = infoMsg .. "; TimeIn(z;ts={a,r,l,d})=" .. buffer(26,2):le_uint()  .. ";" .. buffer(28,2):le_uint() .. "={" .. buffer(24,2):le_uint() .. "," .. buffer(22,2):le_uint() .. "," .. buffer(20,2):le_uint() .. "," .. buffer(18,2):le_uint() .. "}"
	   infoMsg = infoMsg .. "; ZQ=" .. buffer(30,2):le_uint() 
    end
  end

  local infoStr = "Zeo HB BT Msg type=" .. buffer(7,1):uint() .. " " .. msgTypeStr
  if infoMsg:len() > 0 then
    infoStr = infoStr .. "; " .. infoMsg
  end
  if infoAndTreeMsg:len() > 0 then
     infoStr =  infoStr .. "; " .. infoAndTreeMsg
  end
  pinfo.cols.protocol = "Zeo HB BT"
  pinfo.cols.info = infoStr
  
  local subtree = tree:add(zeohbbt_proto,buffer(),infoStr)
  subtree:add(buffer(0,4),"Header: " .. buffer(0,4):string())
  subtree:add(buffer(6,1),"Protocol ver: " .. buffer(6,1):uint())
  subtree:add(buffer(7,1),"Message type: " .. buffer(7,1):uint() .. " " .. msgTypeStr)
  subtree:add(buffer(9,1),"Seq#: " .. buffer(9,1):uint())
  local workStr = "Data content len: " .. buffer(10,2):le_uint()
  if buffer:len() < 13 then
    workStr = workStr .. " [Data content follows in subsequent packets]"
  end
  subtree:add(buffer(10,2),workStr)
  
  if infoAndTreeMsg:len() > 0 then
    subtree:add(buffer(12,1),infoAndTreeMsg)
  end

  if msgType == 10 and buffer:len() >= 20 then
    subtree:add(buffer(12,8),"[Current_time (sec.ms): " .. buffer(12,4):le_uint() .. "." .. buffer(16,4):le_uint() .. "]")
    subtree:add(buffer(12,4),"Current_time_sec: " .. buffer(12,4):le_uint())
    subtree:add(buffer(16,4),"Current_time_plus_ms: " .. buffer(16,4):le_uint())
  elseif msgType == 11 and buffer:len() >= 24 then
    subtree:add(buffer(12,8),"[Current_time (sec.ms): " .. buffer(12,4):le_uint() .. "." .. buffer(16,4):le_uint() .. "]")
    subtree:add(buffer(12,4),"Current_time_sec: " .. buffer(12,4):le_uint())
    subtree:add(buffer(16,4),"Current_time_plus_ms: " .. buffer(16,4):le_uint())
    subtree:add(buffer(20,1),"Is_an_offset: " .. buffer(20,1):uint())
    subtree:add(buffer(21,1),"Offset_is_negative: " .. buffer(21,1):uint())
    subtree:add(buffer(22,1),"Query_SeqNo: " .. buffer(22,1):uint())
    -- pad 1 byte
  elseif msgType == 3 and buffer:len() >= 116 then
    subtree:add(buffer(12,20),"Device_name: " .. buffer(12,20):stringz())
    -- pad 6 bytes
    subtree:add(buffer(38,2),"FlashCalibrationUpdates: " .. buffer(38,2):le_uint())
    subtree:add(buffer(40,2),"FlashSavedDataUpdates: " .. buffer(40,2):le_uint())
    subtree:add(buffer(42,2),"FlashSleepBackupUpdates: " .. buffer(42,2):le_uint())
    subtree:add(buffer(44,4),"Hardware_version: " .. buffer(44,4):le_uint())
    subtree:add(buffer(48,12),"Model: " .. buffer(48,12):stringz())
    subtree:add(buffer(60,16),"Serial_ID: " .. buffer(60,16):stringz())
    subtree:add(buffer(76,4),"Software_version: " .. buffer(76,4):le_uint())
    subtree:add(buffer(80,6),"Headband_address_reversed: " .. tostring(buffer(80,6):ether()))
    -- pad 2 bytes
    subtree:add(buffer(88,20),"Last_Assert_Func: " .. buffer(88,20):stringz())
    subtree:add(buffer(108,4),"Last_Assert_Line: " .. buffer(108,4):le_uint())
    subtree:add(buffer(112,1),"Reset_cause: " .. buffer(112,1):uint())
    subtree:add(buffer(113,1),"Query_response: " .. buffer(113,1):uint())
    subtree:add(buffer(114,2),"Reset_count: " .. buffer(114,2):le_uint())
  end
  if msgType == 9 and buffer:len() >= 28 then
    subtree:add(buffer(12,1),"Active_Forced: " .. buffer(12,1):uint())
    subtree:add(buffer(13,1),"Bluetooth_Locked: " .. buffer(13,1):uint())
    subtree:add(buffer(14,1),"Demo_Mode: " .. buffer(14,1):uint())
    subtree:add(buffer(15,1),"Docked: " .. buffer(15,1):uint())
    subtree:add(buffer(16,1),"On_Head: " .. buffer(16,1):uint())
    subtree:add(buffer(17,1),"Requires_PIN: " .. buffer(17,1):uint())
    subtree:add(buffer(18,1),"Was_Charged: " .. buffer(18,1):uint())
    subtree:add(buffer(19,1),"Was_Queried: " .. buffer(19,1):uint())
    subtree:add(buffer(20,1),"Battery voltage: " .. buffer(20,1):uint())
    subtree:add(buffer(21,1),"Battery state: " .. buffer(21,1):uint())
    subtree:add(buffer(22,1),"Last alarm reason: " .. buffer(22,1):uint())
    subtree:add(buffer(23,1),"Last algorithm mode: " .. buffer(23,1):uint())
    subtree:add(buffer(24,4),"Headband in-use seconds: " .. buffer(24,4):le_uint())
  end
  if msgType == 7 and buffer:len() >= 1156 then
    subtree:add(buffer(12,4),"StartTime: " .. buffer(12,4):le_uint())
    subtree:add(buffer(16,2),"Awakenings: " .. buffer(16,2):le_uint())
    subtree:add(buffer(18,2),"Time_In_Deep_30sec: " .. buffer(18,2):le_uint())
    subtree:add(buffer(20,2),"Time_In_Light_30sec: " .. buffer(20,2):le_uint())
    subtree:add(buffer(22,2),"Time_In_REM_30sec: " .. buffer(22,2):le_uint())
    subtree:add(buffer(24,2),"Time_In_Awake_30sec: " .. buffer(24,2):le_uint())
    subtree:add(buffer(26,2),"Time_To_Z_30sec: " .. buffer(26,2):le_uint())
    subtree:add(buffer(28,2),"Total_Z_30sec: " .. buffer(28,2):le_uint())
    subtree:add(buffer(30,2),"ZQ_Score: " .. buffer(30,2):le_uint())
    subtree:add(buffer(32,1),"Sleep_Valid: " .. buffer(32,1):uint())
    subtree:add(buffer(33,1),"Valid_For_History: " .. buffer(33,1):uint())
    subtree:add(buffer(34,1),"Voltage_Battery: " .. buffer(34,1):uint())
    subtree:add(buffer(35,1),"User_Initiated: " .. buffer(35,1):uint())
    subtree:add(buffer(36,4),"Accumulators_Deep_Sum: " .. buffer(36,4):le_uint())
    subtree:add(buffer(40,2),"Accumulators_Stage1_Counts: " .. buffer(40,2):le_uint())
    subtree:add(buffer(42,2),"Accumulators_Stage2_Counts: " .. buffer(42,2):le_uint())
    subtree:add(buffer(44,2),"Accumulators_Stage3_Counts: " .. buffer(44,2):le_uint())
    subtree:add(buffer(46,2),"Accumulators_Stage4_Counts: " .. buffer(46,2):le_uint())
    subtree:add(buffer(48,2),"Accumulators_Stage5_Counts: " .. buffer(48,2):le_uint())
    subtree:add(buffer(50,2),"Accumulators_Times_Woken: " .. buffer(50,2):le_uint())
    subtree:add(buffer(52,2),"Accumulators_Total_Count: " .. buffer(52,2):le_uint())
    subtree:add(buffer(54,2),"Accumulators_Total_Sleep_Count: " .. buffer(54,2):le_uint())
    subtree:add(buffer(56,2),"Count_To_Z: " .. buffer(56,2):le_uint())
    -- pad 2 bytes
    subtree:add(buffer(60,4),"End_Of_Night: " .. buffer(60,4):le_uint())
    -- displayHypnogram 108 bytes
    subtree:add(buffer(172,1),"Insufficient_Data: " .. buffer(172,1):uint())
    -- pad 3 bytes
    subtree:add(buffer(176,4),"Insufficient_Data_Startime: " .. buffer(176,4):le_uint())
    subtree:add(buffer(180,2),"Light_Changed_to_Deep: " .. buffer(180,2):le_uint())
    -- pad 2 bytes
    subtree:add(buffer(184,4),"Start_Of_Night: " .. buffer(184,4):le_uint())
    subtree:add(buffer(188,1),"Valid: " .. buffer(188,1):uint())
    -- pad 3 bytes
    -- displayHypnogram 964 bytes
  end
end

frameDirection_f = Field.new("frame.p2p_dir")

-- declare our protocol
zeohbbt_proto = Proto("zeohbbt","Zeo Headband Bluetooth")
-- one-time initialization
function zeohbbt_proto.init()
  mReassembly_Buffer = nil
  mReassembly_LenNeeded = 0
  mReassembly_LenSoFar = 0
  mReassembly_MsgType = 0
  mReassembly_MsgTypeStr = ""
  mReassembly_Dir = 0
  mReassembly = 0
  mReassembled = {}
end
-- create a callback function to dissect it
function zeohbbt_proto.dissector(buffer,pinfo,tree)
  if buffer:len() == 0 then
    -- skip packets with no payload
    return 0
  end
  
  -- has this packet already been reassembled?
  local state = mReassembled[pinfo.number]
  if state ~= nil then
    if state.type == 3 then
      local lBuf = ByteArray.tvb(state.buffer, "ReAssembled")
      zeohbbt_fullDecode(lBuf,pinfo,tree,state.lenNeeded,state.msgTypeStr)
      return
    elseif state.type == 2 then
      zeohbbt_part2Decode(buffer,pinfo,tree,state.msgType,state.lenNeeded,state.msgTypeStr)
      return
    elseif state.type == 1 then
      zeohbbt_part1Decode(buffer,pinfo,tree,state.lenNeeded,state.msgTypeStr)
      return
    end
  end
  
  -- either first time pass through the capture else the packet did not need reassembly
  if buffer:len() >= 4 and buffer(0,4):string() == "HMSG" then
  elseif mReassembly ~= 0 then
    -- packet reassembly into a complete PDU is in-progress
    if frameDirection_f().value ~=  mReassembly_Dir then
    elseif mReassembly_LenSoFar + buffer:len() >= mReassembly_LenNeeded then
      -- reassembly is complete
      mReassembly_Buffer:append(buffer(0):bytes())
      mReassembly_LenSoFar = mReassembly_LenSoFar + buffer:len()
      local lBuf = ByteArray.tvb(mReassembly_Buffer,"Assembled")
      zeohbbt_fullDecode(lBuf,pinfo,tree,mReassembly_LenNeeded,mReassembly_MsgTypeStr)
      state = {}
      state.type = 3
      state.buffer = mReassembly_Buffer
      state.lenNeeded =  mReassembly_LenNeeded
      state.msgType = mReassembly_MsgType
      state.msgTypeStr = mReassembly_MsgTypeStr
      mReassembled[pinfo.number] = state
      mReassembly = 0
      mReassembly_Buffer = nil
      return 
    else
      -- continue reassembly
      mReassembly_Buffer:append(buffer(0):bytes())
      mReassembly_LenSoFar = mReassembly_LenSoFar + buffer:len()
      zeohbbt_part2Decode(buffer,pinfo,tree,mReassembly_MsgType,mReassembly_LenNeeded,mReassembly_MsgTypeStr)
      state = {}
      state.type = 2
      state.lenNeeded =  mReassembly_LenNeeded
      state.msgType = mReassembly_MsgType
      state.msgTypeStr = mReassembly_MsgTypeStr
      mReassembled[pinfo.number] = state
      return
    end
  end
  
  -- new PDU to dissect; identify it and see if its ours
  local msgTypeLen, msgTypeStr  = zeohbbt_identify(buffer)
  if msgTypeLen < 0 then 
    return 0
  end
  
  -- it is ours; dissect if it is complete PDU
  if buffer:len() >= msgTypeLen then
    zeohbbt_fullDecode(buffer,pinfo,tree,msgTypeLen,msgTypeStr)
    return
  end
  
  -- packet is not a complete PDU; need to activate re-assembly
  zeohbbt_part1Decode(buffer,pinfo,tree,msgTypeLen,msgTypeStr)
  mReassembly = 1
  mReassembly_Dir = frameDirection_f().value
  mReassembly_MsgType = buffer(7,1):uint()
  mReassembly_MsgTypeStr = msgTypeStr
  mReassembly_LenNeeded = msgTypeLen
  mReassembly_LenSoFar = buffer:len()
  mReassembly_Buffer = buffer(0):bytes()
  state = {}
  state.type = 1
  state.lenNeeded =  mReassembly_LenNeeded
  state.msgType = mReassembly_MsgType
  state.msgTypeStr = mReassembly_MsgTypeStr
  mReassembled[pinfo.number] = state
  return
end

-- load the btrfcomm from the master Dissector Tables
-- then register our protocol into RFCOMM
tmpTable = DissectorTable.get("btrfcomm.dlci")
tmpTable:add("0-31",zeohbbt_proto)
