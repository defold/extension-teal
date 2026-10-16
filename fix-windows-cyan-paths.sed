/^   local res = table.concat(self, path.separator)$/a\
   if #self == 1 and self:is_absolute() then\
      return res .. path.separator\
   end
